package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.toptan.model.Urun
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

    // --- KATALOGDAN SEPETE ÜRÜN EKLEME FONKSİYONU ---
    fun sepeteEkle(urun: Urun) {
        val mevcutListe = _sepet.value.toMutableList()

        // 1. GÜVENLİK: FARKLI TOPTANCI KONTROLÜ
        if (mevcutListe.isNotEmpty()) {
            val sepettekiToptanciId = mevcutListe.first().urun.toptanciId
            if (urun.toptanciId != sepettekiToptanciId) {
                _siparisMesaji.value = "Sepetinizde farklı bir toptancıya ait ürün var. Lütfen önce mevcut sepetinizi temizleyin."
                return
            }
        }

        // 2. GÜVENLİK: MİNİMUM ALIM VE STOK KONTROLÜ
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
                    _siparisMesaji.value = "Mevcut stok miktarını aşamazsınız!"
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

    private fun hesaplaToplamTutar() {
        _toplamTutar.value = _sepet.value.sumOf { it.urun.fiyat * it.secilenMiktar }
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

        _siparisMesaji.value = "Cari limitiniz kontrol ediliyor..."

        val kullaniciRef = firestore.collection("kullanicilar").document(aktifKullanici.uid)

        kullaniciRef.get().addOnSuccessListener { snapshot ->
            val mevcutLimit = if (snapshot.exists() && snapshot.contains("cariLimit")) {
                snapshot.getDouble("cariLimit") ?: 50000.0
            } else {
                50000.0
            }

            if (toplamTutar > mevcutLimit) {
                _siparisMesaji.value = "Hata: Cari limitiniz yetersiz! (Kalan Limit: ${mevcutLimit} ₺)"
                return@addOnSuccessListener
            }

            _siparisMesaji.value = "Siparişiniz işleniyor..."
            val batch = firestore.batch()

            // Cari limiti düş
            batch.update(kullaniciRef, "cariLimit", FieldValue.increment(-toplamTutar))
            if (!snapshot.exists() || !snapshot.contains("cariLimit")) {
                batch.set(kullaniciRef, hashMapOf("cariLimit" to (50000.0 - toplamTutar)), com.google.firebase.firestore.SetOptions.merge())
            }

            // Firebase'den müşterinin şirket VE İLETİŞİM bilgilerini okuyoruz
            val musteriSirketUnvani = snapshot.getString("sirketUnvani") ?: "Belirtilmemiş Şirket"
            val musteriAdresi = snapshot.getString("adres") ?: "Adres Belirtilmemiş"
            val musteriVergiNo = snapshot.getString("vergiNo") ?: "-"
            val musteriVergiDairesi = snapshot.getString("vergiDairesi") ?: "-"
            // --- YENİ EKLENEN İLETİŞİM BİLGİLERİ ---
            val musteriYetkili = snapshot.getString("yetkiliKisi") ?: "Belirtilmemiş"
            val musteriTelefon = snapshot.getString("telefon") ?: "Belirtilmemiş"

            // Sipariş belgesini oluştur ve Tüm Bilgileri Ekle
            val siparisRef = firestore.collection("siparisler").document()
            val yeniSiparis = hashMapOf(
                "siparisId" to siparisRef.id,
                "musteriUid" to aktifKullanici.uid,
                "musteriEmail" to (aktifKullanici.email ?: "Bilinmiyor"),
                "sirketUnvani" to musteriSirketUnvani,
                "teslimatAdresi" to musteriAdresi,
                "vergiNo" to musteriVergiNo,
                "vergiDairesi" to musteriVergiDairesi,
                // --- YENİ EKLENEN İLETİŞİM BİLGİLERİ ---
                "yetkiliKisi" to musteriYetkili,
                "telefon" to musteriTelefon,

                "toptanciId" to toptanciId,
                "siparisOzeti" to sepetOzet,
                "toplamTutar" to toplamTutar,
                "durum" to "Hazırlanıyor",
                "tarih" to System.currentTimeMillis()
            )
            batch.set(siparisRef, yeniSiparis)

            // Stokları Düş
            for (oge in _sepet.value) {
                val urunRef = firestore.collection("urunler").document(oge.urun.id)
                batch.update(urunRef, "stok", FieldValue.increment(-oge.secilenMiktar.toLong()))
                batch.update(urunRef, "stokMiktari", FieldValue.increment(-oge.secilenMiktar.toLong()))
            }

            // İşlemleri Ateşle
            batch.commit()
                .addOnSuccessListener {
                    _siparisBasarili.value = true
                    _siparisMesaji.value = "Siparişiniz Açık Hesabınıza (Cari) işlenerek onaylandı!"
                    _sepet.value = emptyList()
                    hesaplaToplamTutar()
                }
                .addOnFailureListener { hata ->
                    _siparisMesaji.value = "Sipariş tamamlanırken hata oluştu: ${hata.message}"
                }
        }.addOnFailureListener {
            _siparisMesaji.value = "Kullanıcı bilgileri alınamadı."
        }
    }

    fun mesajiTemizle() {
        _siparisMesaji.value = null
    }

    fun sepetiTemizle() {
        _sepet.value = emptyList()
        hesaplaToplamTutar()
    }

    fun urunuSil(silinecekUrunId: String) {
        _sepet.value = _sepet.value.filter { it.urun.id != silinecekUrunId }
        hesaplaToplamTutar()
    }

    fun siparisBasariliDurumunuSifirla() {
        _siparisBasarili.value = false
    }
}