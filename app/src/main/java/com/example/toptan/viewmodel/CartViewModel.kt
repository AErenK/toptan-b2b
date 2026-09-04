package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.toptan.model.Urun // Kendi modelini import ettik
import kotlinx.coroutines.flow.asStateFlow

// Sepet öğesi mantığı (ViewModel içinde kalabilir)
data class SepetOgesi(val urun: Urun, var secilenMiktar: Int)

class CartViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _sepet = MutableStateFlow<List<SepetOgesi>>(emptyList()) // Başlangıçta boş
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
        // Ürün sepette zaten var mı kontrol et
        val index = mevcutListe.indexOfFirst { it.urun.id == urun.id }

        if (index != -1) {
            // Varsa, üzerine minimum alım miktarı kadar ekle
            val eskiOge = mevcutListe[index]
            mevcutListe[index] = eskiOge.copy(secilenMiktar = eskiOge.secilenMiktar + urun.minAlimMiktari)
        } else {
            // Yoksa, sepet listesine minimum alım miktarıyla yeni öğe olarak ekle
            mevcutListe.add(SepetOgesi(urun, urun.minAlimMiktari))
        }

        _sepet.value = mevcutListe
        hesaplaToplamTutar()
    }

    // Dikkat: urunId artık Int değil, String (Senin modeline göre)
    fun miktarArtir(urunId: String) {
        _sepet.value = _sepet.value.map {
            if (it.urun.id == urunId) it.copy(secilenMiktar = it.secilenMiktar + 1) else it
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

    // --- FİREBASE SİPARİŞ GÖNDERME ---
    // --- FİREBASE SİPARİŞ GÖNDERME ---
    fun siparisiTamamla(toplamTutar: Double, sepetOzet: String) {
        val aktifKullanici = auth.currentUser
        if (aktifKullanici == null) {
            _siparisMesaji.value = "Hata: Oturum açmadan sipariş veremezsiniz."
            return
        }

        // Sepetteki ilk ürünün toptanciId'sini al (Zaten sepetteki tüm ürünler aynı toptancıya aittir)
        val toptanciId = _sepet.value.firstOrNull()?.urun?.toptanciId

        if (toptanciId == null) {
            _siparisMesaji.value = "Hata: Sipariş verilecek toptancı bulunamadı."
            return
        }

        _siparisMesaji.value = "Siparişiniz buluta gönderiliyor..."

        val siparisRef = firestore.collection("siparisler").document()
        val yeniSiparis = hashMapOf(
            "siparisId" to siparisRef.id,
            "musteriUid" to aktifKullanici.uid,
            "musteriEmail" to (aktifKullanici.email ?: "Bilinmiyor"),
            "toptanciId" to toptanciId, // KRİTİK EKLENTİ: Hangi toptancıya gittiği
            "siparisOzeti" to sepetOzet,
            "toplamTutar" to toplamTutar,
            "durum" to "Hazırlanıyor",
            "tarih" to System.currentTimeMillis()
        )

        siparisRef.set(yeniSiparis)
            .addOnSuccessListener {
                _siparisBasarili.value = true // YENİ: Başarılı ekranını tetikler
                _siparisMesaji.value = "Sipariş Başarıyla Oluşturuldu!"
                _sepet.value = emptyList() // Sipariş sonrası sepeti temizle
                hesaplaToplamTutar()
            }
            .addOnFailureListener { hata ->
                _siparisMesaji.value = "Sipariş başarısız oldu: ${hata.message}"
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
        // Listeyi filtreleyip, id'si eşleşmeyenleri (yani silinmeyecekleri) tutuyoruz
        _sepet.value = _sepet.value.filter { it.urun.id != silinecekUrunId }
        hesaplaToplamTutar()
    }

    fun siparisBasariliDurumunuSifirla() {
        _siparisBasarili.value = false
    }

}