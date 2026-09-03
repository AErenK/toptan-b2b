package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Urun
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class ToptanciViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj

    fun urunEkle(ad: String, fiyat: String, minAlim: String, stok: String) {
        val toptanciId = auth.currentUser?.uid
        if (toptanciId == null) {
            _mesaj.value = "Hata: Oturum bulunamadı."
            return
        }

        // Boş alan kontrolü
        if (ad.isEmpty() || fiyat.isEmpty() || minAlim.isEmpty() || stok.isEmpty()) {
            _mesaj.value = "Lütfen tüm alanları doldurun."
            return
        }

        // Metinleri sayılara çevir (Hatalı harf girildiyse yakala)
        val fiyatDouble = fiyat.toDoubleOrNull()
        val minAlimInt = minAlim.toIntOrNull()
        val stokInt = stok.toIntOrNull()

        if (fiyatDouble == null || minAlimInt == null || stokInt == null) {
            _mesaj.value = "Fiyat, Min Alım ve Stok alanlarına sadece sayı giriniz."
            return
        }

        _mesaj.value = "Ürün ekleniyor..."

        // Rastgele bir Ürün ID oluşturuyoruz
        val urunId = UUID.randomUUID().toString()

        // Veritabanına gidecek HashMap'i hazırlıyoruz
        val yeniUrun = hashMapOf(
            "id" to urunId,
            "toptanciId" to toptanciId,
            "ad" to ad,
            "fiyat" to fiyatDouble,
            "minAlimMiktari" to minAlimInt,
            "stokMiktari" to stokInt,
            "eklenmeTarihi" to System.currentTimeMillis()
        )

        // Firestore "urunler" koleksiyonuna yaz
        firestore.collection("urunler").document(urunId).set(yeniUrun)
            .addOnSuccessListener {
                _mesaj.value = "Ürün başarıyla kataloğa eklendi!"
            }
            .addOnFailureListener { hata ->
                _mesaj.value = "Ürün eklenemedi: ${hata.message}"
            }
    }

    fun mesajiTemizle() {
        _mesaj.value = null
    }
}