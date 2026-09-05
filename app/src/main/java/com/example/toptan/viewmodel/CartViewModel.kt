package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue // YENİ: Atomik işlemler için
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
        if (urun.stok < urun.minAlimMiktari) {
            _siparisMesaji.value = "Bu ürün için yeterli stok bulunmuyor."
            return
        }

        val mevcutListe = _sepet.value.toMutableList()
        val index = mevcutListe.indexOfFirst { it.urun.id == urun.id }

        if (index != -1) {
            val eskiOge = mevcutListe[index]
            val yeniMiktar = eskiOge.secilenMiktar + urun.minAlimMiktari

            // YENİ: Eklenecek miktar stoğu aşıyor mu kontrolü
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
                // YENİ: Artırma işleminde stok sınırı kontrolü
                if (it.secilenMiktar + 1 <= it.urun.stok) {
                    it.copy(secilenMiktar = it.secilenMiktar + 1)
                } else {
                    _siparisMesaji.value = "Mevcut stok miktarını aşamazsınız!"
                    it // Değişiklik yapmadan geri döndür
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

    // --- FİREBASE SİPARİŞ GÖNDERME VE OTOMATİK STOK DÜŞME ---
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

        _siparisMesaji.value = "Siparişiniz işleniyor..."

        // YENİ: BATCH (Toplu İşlem) başlatıyoruz.
        // Bu sayede sipariş kaydı ve stok düşme işlemleri birbirine bağlanır.
        val batch = firestore.batch()

        // 1. İşlem: Sipariş belgesini oluştur
        val siparisRef = firestore.collection("siparisler").document()
        val yeniSiparis = hashMapOf(
            "siparisId" to siparisRef.id,
            "musteriUid" to aktifKullanici.uid,
            "musteriEmail" to (aktifKullanici.email ?: "Bilinmiyor"),
            "toptanciId" to toptanciId,
            "siparisOzeti" to sepetOzet,
            "toplamTutar" to toplamTutar,
            "durum" to "Hazırlanıyor",
            "tarih" to System.currentTimeMillis()
        )
        batch.set(siparisRef, yeniSiparis)

        // 2. İşlem: Sepetteki her ürünün stok miktarını Firebase'de atomik olarak azalt
        val mevcutSepet = _sepet.value
        for (oge in mevcutSepet) {
            val urunRef = firestore.collection("urunler").document(oge.urun.id)
            // FieldValue.increment(negatif_değer) kullanarak stoğu anlık düşürürüz
            batch.update(urunRef, "stok", FieldValue.increment(-oge.secilenMiktar.toLong()))
            // Modelinde stokMiktari da olduğu için güvenliğe karşı onu da güncelleyelim:
            batch.update(urunRef, "stokMiktari", FieldValue.increment(-oge.secilenMiktar.toLong()))
        }

        // 3. İşlem: Tüm Batch işlemlerini tek seferde veritabanına gönder
        batch.commit()
            .addOnSuccessListener {
                _siparisBasarili.value = true
                _siparisMesaji.value = "Sipariş Başarıyla Oluşturuldu!"
                _sepet.value = emptyList() // Siparişi verdikten sonra sepeti temizle
                hesaplaToplamTutar()
            }
            .addOnFailureListener { hata ->
                _siparisMesaji.value = "Sipariş tamamlanırken hata oluştu: ${hata.message}"
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