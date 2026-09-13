package com.example.toptan.viewmodel.musteri

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

    private val _iskontoOrani = MutableStateFlow(0.0)
    val iskontoOrani: StateFlow<Double> = _iskontoOrani.asStateFlow()

    private val _toptanciMinLimit = MutableStateFlow(0.0)
    val toptanciMinLimit: StateFlow<Double> = _toptanciMinLimit.asStateFlow()

    init {
        kullaniciBilgileriniDinle()
    }

    private fun kullaniciBilgileriniDinle() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("kullanicilar").document(uid).addSnapshotListener { snap, _ ->
            if (snap != null && snap.exists()) {
                _iskontoOrani.value = snap.getDouble("iskontoOrani") ?: 0.0
                hesaplaToplamTutar()
            }
        }
    }

    private fun toptanciKuraliniGetir(toptanciId: String) {
        firestore.collection("kullanicilar").document(toptanciId).get().addOnSuccessListener { snap ->
            if (snap.exists()) {
                _toptanciMinLimit.value = snap.getDouble("minSiparisTutari") ?: 0.0
            }
        }
    }

    fun sepeteEkle(urun: Urun) {
        val mevcutListe = _sepet.value.toMutableList()

        if (mevcutListe.isEmpty()) {
            toptanciKuraliniGetir(urun.toptanciId)
        } else {
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

        val limit = _toptanciMinLimit.value
        if (toplamTutar < limit) {
            _siparisMesaji.value = "Hata: Bu toptancının minimum sipariş limiti $limit ₺'dir."
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
                _siparisMesaji.value = "Hata: Cari limitiniz yetersiz! (Kalan Limit: $mevcutLimit ₺)"
                return@addOnSuccessListener
            }

            _siparisMesaji.value = "Siparişiniz işleniyor..."
            val batch = firestore.batch()

            batch.update(kullaniciRef, "cariLimit", FieldValue.increment(-toplamTutar))

            val siparisRef = firestore.collection("siparisler").document()
            val yeniSiparis = hashMapOf(
                "siparisId" to siparisRef.id,
                "musteriUid" to aktifKullanici.uid,
                "musteriEmail" to (aktifKullanici.email ?: "Bilinmiyor"),
                "sirketUnvani" to (snapshot.getString("sirketUnvani") ?: "Belirtilmemiş Şirket"),
                "teslimatAdresi" to (snapshot.getString("adres") ?: "Adres Belirtilmemiş"),
                "vergiNo" to (snapshot.getString("vergiNo") ?: "-"),
                "vergiDairesi" to (snapshot.getString("vergiDairesi") ?: "-"),
                "yetkiliKisi" to (snapshot.getString("yetkiliKisi") ?: "Belirtilmemiş"),
                "telefon" to (snapshot.getString("telefon") ?: "Belirtilmemiş"),
                "toptanciId" to toptanciId,
                "siparisOzeti" to sepetOzet,
                "toplamTutar" to toplamTutar,
                "uygulananIskonto" to _iskontoOrani.value,
                "durum" to "Hazırlanıyor",
                "tarih" to System.currentTimeMillis()
            )
            batch.set(siparisRef, yeniSiparis)

            for (oge in _sepet.value) {
                val urunRef = firestore.collection("urunler").document(oge.urun.id)
                batch.update(urunRef, "stok", FieldValue.increment(-oge.secilenMiktar.toLong()))
            }

            batch.commit().addOnSuccessListener {
                _siparisBasarili.value = true
                _siparisMesaji.value = "Siparişiniz Açık Hesabınıza (Cari) işlenerek onaylandı."
                sepetiTemizle()
            }.addOnFailureListener { hata ->
                _siparisMesaji.value = "Sipariş tamamlanırken hata oluştu: ${hata.message}"
            }
        }.addOnFailureListener { _siparisMesaji.value = "Kullanıcı bilgileri alınamadı." }
    }

    fun gecmisSiparisiTekrarla(siparisOzeti: String, toptanciId: String, onSuccess: () -> Unit) {
        _siparisMesaji.value = "Ürünlerin güncel durumları kontrol ediliyor..."
        firestore.collection("urunler")
            .whereEqualTo("toptanciId", toptanciId)
            .whereEqualTo("aktifMi", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val guncelUrunler = snapshot.documents.mapNotNull { it.toObject(Urun::class.java)?.copy(id = it.id) }
                val yeniSepet = mutableListOf<SepetOgesi>()
                var bulunamayanUrunler = false

                val regex = Regex("(\\d+)x\\s(.*?)(?:,|$)")
                val matches = regex.findAll(siparisOzeti)

                for (match in matches) {
                    val eskiAdet = match.groupValues[1].toIntOrNull() ?: continue
                    val urunAdi = match.groupValues[2].trim()
                    val eslesenUrun = guncelUrunler.find { it.ad.equals(urunAdi, ignoreCase = true) }

                    if (eslesenUrun != null && eslesenUrun.stok > 0) {
                        var eklenecekAdet = if (eskiAdet > eslesenUrun.stok) eslesenUrun.stok else eskiAdet
                        if (eklenecekAdet < eslesenUrun.minAlimMiktari) eklenecekAdet = eslesenUrun.minAlimMiktari

                        if (eklenecekAdet <= eslesenUrun.stok) {
                            yeniSepet.add(SepetOgesi(eslesenUrun, eklenecekAdet))
                        } else { bulunamayanUrunler = true }
                    } else { bulunamayanUrunler = true }
                }

                if (yeniSepet.isNotEmpty()) {
                    _sepet.value = yeniSepet
                    hesaplaToplamTutar()
                    toptanciKuraliniGetir(toptanciId) // Sepeti doldurunca limiti de çek
                    if (bulunamayanUrunler) _siparisMesaji.value = "Sipariş sepete kopyalandı ancak bazı ürünler stokta olmadığı için eksik eklendi."
                    else _siparisMesaji.value = "Sipariş başarıyla sepetinize kopyalandı!"
                    onSuccess()
                } else {
                    _siparisMesaji.value = "Bu siparişteki ürünlerin hiçbiri şu an stokta veya satışta değil."
                }
            }
            .addOnFailureListener { _siparisMesaji.value = "Katalog kontrol edilirken hata oluştu." }
    }

    fun mesajiTemizle() { _siparisMesaji.value = null }
    fun sepetiTemizle() { _sepet.value = emptyList(); _toptanciMinLimit.value = 0.0; hesaplaToplamTutar() }
    fun urunuSil(silinecekUrunId: String) {
        val yeniListe = _sepet.value.filter { it.urun.id != silinecekUrunId }
        _sepet.value = yeniListe
        if (yeniListe.isEmpty()) _toptanciMinLimit.value = 0.0
        hesaplaToplamTutar()
    }
    fun siparisBasariliDurumunuSifirla() { _siparisBasarili.value = false }
}