package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.toptan.model.Urun // Kendi modelini import ettik

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
    fun siparisiTamamla(toplamTutar: Double, sepetOzet: String) {
        val aktifKullanici = auth.currentUser
        if (aktifKullanici == null) {
            _siparisMesaji.value = "Hata: Oturum açmadan sipariş veremezsiniz."
            return
        }

        _siparisMesaji.value = "Siparişiniz buluta gönderiliyor..."

        val siparisRef = firestore.collection("siparisler").document()
        val yeniSiparis = hashMapOf(
            "siparisId" to siparisRef.id,
            "musteriUid" to aktifKullanici.uid,
            "musteriEmail" to (aktifKullanici.email ?: "Bilinmiyor"),
            "siparisOzeti" to sepetOzet,
            "toplamTutar" to toplamTutar,
            "durum" to "Hazırlanıyor",
            "tarih" to System.currentTimeMillis()
        )

        siparisRef.set(yeniSiparis)
            .addOnSuccessListener {
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
}