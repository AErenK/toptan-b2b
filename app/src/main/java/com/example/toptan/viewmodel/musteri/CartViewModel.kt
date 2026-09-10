package com.example.toptan.viewmodel.musteri

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.toptan.model.Urun
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.asStateFlow

data class SepetOgesi(val urun: Urun, var secilenMiktar: Int)

class CartViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _sepet = MutableStateFlow<List<SepetOgesi>>(emptyList())
    val sepet: StateFlow<List<SepetOgesi>> = _sepet

    private val _toplamTutar = MutableStateFlow(0.0)
    val toplamTutar: StateFlow<Double> = _toplamTutar

    private val _siparisMesaji = MutableStateFlow<String?>(null)
    val siparisMesaji: StateFlow<String?> = _siparisMesaji

    private val _siparisBasarili = MutableStateFlow(false)
    val siparisBasarili: StateFlow<Boolean> = _siparisBasarili.asStateFlow()

    // YENİ: Müşterinin İskonto Oranı
    private val _iskontoOrani = MutableStateFlow(0.0)
    val iskontoOrani: StateFlow<Double> = _iskontoOrani.asStateFlow()

    init {
        kullaniciBilgileriniDinle()
    }

    // YENİ: Müşterinin iskonto oranını Firebase'den anlık dinliyoruz
    private fun kullaniciBilgileriniDinle() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("kullanicilar").document(uid).addSnapshotListener { snap, _ ->
            if (snap != null && snap.exists()) {
                _iskontoOrani.value = snap.getDouble("iskontoOrani") ?: 0.0
                hesaplaToplamTutar() // İskonto değişirse sepetteki toplam tutarı anında yeniden hesapla
            }
        }
    }

    fun sepeteEkle(urun: Urun) {
        val mevcutListe = _sepet.value.toMutableList()

        if (mevcutListe.isNotEmpty()) {
            val sepettekiToptanciId = mevcutListe.first().urun.toptanciId
            if (urun.toptanciId != sepettekiToptanciId) {
                _siparisMesaji.value = "Sepetinizde farklı bir toptancıya ait ürün var. Lütfen önce mevcut sepetinizi temizleyin."
                return
            }
        }

        if (urun.stok < urun.minAlimMiktari) {
            _siparisMesaji.value = "Bu ürün için yeterli stok bulunmuyor."
            return
        }

        val index = mevcutListe.indexOfFirst { it.urun.id == urun.id }
        if (index != -1) {
            val eskiOge = mevcutListe[index]
            val yeniMiktar = eskiOge.secilenMiktar + urun.minAlimMiktari
            if (yeniMiktar > urun.stok) {
                _siparisMesaji.value = "Stok limitine ulaştınız. Daha fazla ekleyemezsiniz."
            } else {
                mevcutListe[index] = eskiOge.copy(secilenMiktar = yeniMiktar)
            }
        } else {
            mevcutListe.add(SepetOgesi(urun, urun.minAlimMiktari))
        }

        _sepet.value = mevcutListe
        hesaplaToplamTutar()
    }

    fun miktarArtir(urunId: String) {
        _sepet.value = _sepet.value.map {
            if (it.urun.id == urunId) {
                if (it.secilenMiktar + 1 <= it.urun.stok) {
                    it.copy(secilenMiktar = it.secilenMiktar + 1)
                } else {
                    _siparisMesaji.value = "Mevcut stok miktarını aşamazsınız."
                    it
                }
            } else it
        }
        hesaplaToplamTutar()
    }

    fun miktarAzalt(urunId: String) {
        _sepet.value = _sepet.value.map {
            if (it.urun.id == urunId && it.secilenMiktar > it.urun.minAlimMiktari) {
                it.copy(secilenMiktar = it.secilenMiktar - 1)
            } else it
        }
        hesaplaToplamTutar()
    }

    // YENİ: Toplam tutarı hesaplarken İskonto Oranını da denkleme katıyoruz
    private fun hesaplaToplamTutar() {
        val oran = _iskontoOrani.value
        _toplamTutar.value = _sepet.value.sumOf {
            val indirimliFiyat = it.urun.fiyat * (1 - (oran / 100.0))
            indirimliFiyat * it.secilenMiktar
        }
    }

    fun siparisiTamamla(toplamTutar: Double, sepetOzet: String) {
        val aktifKullanici = auth.currentUser
        if (aktifKullanici == null) {
            _siparisMesaji.value = "Hata: Oturum açmadan sipariş veremezsiniz."
            return
        }

        val toptanciId = _sepet.value.firstOrNull()?.urun?.toptanciId
        if (toptanciId == null) {
            _siparisMesaji.value = "Hata: Sipariş verilecek toptancı bulunamadı."
            return
        }

        _siparisMesaji.value = "Toptancı kuralları kontrol ediliyor..."

        firestore.collection("kullanicilar").document(toptanciId).get().addOnSuccessListener { toptanciSnap ->
            val minSiparisTutari = toptanciSnap.getDouble("minSiparisTutari") ?: 0.0

            if (toplamTutar < minSiparisTutari) {
                _siparisMesaji.value = "Hata: Bu toptancının minimum sipariş limiti $minSiparisTutari ₺'dir."
                return@addOnSuccessListener
            }

            _siparisMesaji.value = "Cari limitiniz kontrol ediliyor..."
            val kullaniciRef = firestore.collection("kullanicilar").document(aktifKullanici.uid)

            kullaniciRef.get().addOnSuccessListener { snapshot ->
                val mevcutLimit = if (snapshot.exists() && snapshot.contains("cariLimit")) {
                    snapshot.getDouble("cariLimit") ?: 50000.0
                } else {
                    50000.0
                }

                if (toplamTutar > mevcutLimit) {
                    _siparisMesaji.value = "Hata: Cari limitiniz yetersiz! (Kalan Limit: $mevcutLimit ₺)"
                    return@addOnSuccessListener
                }

                _siparisMesaji.value = "Siparişiniz işleniyor..."
                val batch = firestore.batch()

                batch.update(kullaniciRef, "cariLimit", FieldValue.increment(-toplamTutar))

                val musteriSirketUnvani = snapshot.getString("sirketUnvani") ?: "Belirtilmemiş Şirket"
                val musteriAdresi = snapshot.getString("adres") ?: "Adres Belirtilmemiş"
                val musteriVergiNo = snapshot.getString("vergiNo") ?: "-"
                val musteriVergiDairesi = snapshot.getString("vergiDairesi") ?: "-"
                val musteriYetkili = snapshot.getString("yetkiliKisi") ?: "Belirtilmemiş"
                val musteriTelefon = snapshot.getString("telefon") ?: "Belirtilmemiş"

                val siparisRef = firestore.collection("siparisler").document()
                val yeniSiparis = hashMapOf(
                    "siparisId" to siparisRef.id,
                    "musteriUid" to aktifKullanici.uid,
                    "musteriEmail" to (aktifKullanici.email ?: "Bilinmiyor"),
                    "sirketUnvani" to musteriSirketUnvani,
                    "teslimatAdresi" to musteriAdresi,
                    "vergiNo" to musteriVergiNo,
                    "vergiDairesi" to musteriVergiDairesi,
                    "yetkiliKisi" to musteriYetkili,
                    "telefon" to musteriTelefon,
                    "toptanciId" to toptanciId,
                    "siparisOzeti" to sepetOzet,
                    "toplamTutar" to toplamTutar,
                    "uygulananIskonto" to _iskontoOrani.value, // YENİ: Makbuza not düşüyoruz
                    "durum" to "Hazırlanıyor",
                    "tarih" to System.currentTimeMillis()
                )
                batch.set(siparisRef, yeniSiparis)

                for (oge in _sepet.value) {
                    val urunRef = firestore.collection("urunler").document(oge.urun.id)
                    batch.update(urunRef, "stok", FieldValue.increment(-oge.secilenMiktar.toLong()))
                }

                batch.commit()
                    .addOnSuccessListener {
                        _siparisBasarili.value = true
                        _siparisMesaji.value = "Siparişiniz Açık Hesabınıza (Cari) işlenerek onaylandı."
                        _sepet.value = emptyList()
                        hesaplaToplamTutar()
                    }
                    .addOnFailureListener { hata ->
                        _siparisMesaji.value = "Sipariş tamamlanırken hata oluştu: ${hata.message}"
                    }
            }.addOnFailureListener { _siparisMesaji.value = "Kullanıcı bilgileri alınamadı." }
        }.addOnFailureListener { _siparisMesaji.value = "Toptancı kuralları alınamadı." }
    }

    fun mesajiTemizle() { _siparisMesaji.value = null }
    fun sepetiTemizle() { _sepet.value = emptyList(); hesaplaToplamTutar() }
    fun urunuSil(silinecekUrunId: String) { _sepet.value = _sepet.value.filter { it.urun.id != silinecekUrunId }; hesaplaToplamTutar() }
    fun siparisBasariliDurumunuSifirla() { _siparisBasarili.value = false }
}