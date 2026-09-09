package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Müşteri verilerini tutacağımız model
data class Musteri(
    val uid: String,
    val sirketUnvani: String,
    val yetkiliKisi: String,
    val telefon: String,
    val email: String,
    val vergiNo: String,
    val vergiDairesi: String,
    val cariLimit: Double
)

class ToptanciMusterilerViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _musteriler = MutableStateFlow<List<Musteri>>(emptyList())
    val musteriler: StateFlow<List<Musteri>> = _musteriler

    private val _yukleniyor = MutableStateFlow(true)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj

    init {
        musterileriGetir()
    }

    // Sadece rolü "musteri" olanları Firebase'den çeker
    fun musterileriGetir() {
        _yukleniyor.value = true
        firestore.collection("kullanicilar")
            .whereEqualTo("rol", "musteri")
            .get()
            .addOnSuccessListener { snapshot ->
                val musteriListesi = snapshot.documents.map { doc ->
                    Musteri(
                        uid = doc.id,
                        sirketUnvani = doc.getString("sirketUnvani") ?: "Belirtilmemiş",
                        yetkiliKisi = doc.getString("yetkiliKisi") ?: "-",
                        telefon = doc.getString("telefon") ?: "-",
                        email = doc.getString("email") ?: "-",
                        vergiNo = doc.getString("vergiNo") ?: "-",
                        vergiDairesi = doc.getString("vergiDairesi") ?: "-",
                        cariLimit = doc.getDouble("cariLimit") ?: 50000.0 // Varsayılan limit
                    )
                }
                _musteriler.value = musteriListesi
                _yukleniyor.value = false
            }
            .addOnFailureListener {
                _mesaj.value = "Müşteriler yüklenirken hata oluştu."
                _yukleniyor.value = false
            }
    }

    // Seçilen müşterinin cari limitini Firebase'de günceller
    fun cariLimitGuncelle(musteriUid: String, yeniLimit: Double) {
        firestore.collection("kullanicilar").document(musteriUid)
            .update("cariLimit", yeniLimit)
            .addOnSuccessListener {
                _mesaj.value = "Cari limit başarıyla güncellendi! (başarılı)"
                musterileriGetir() // Listeyi tazele
            }
            .addOnFailureListener {
                _mesaj.value = "Limit güncellenemedi."
            }
    }

    fun mesajiTemizle() {
        _mesaj.value = null
    }
}