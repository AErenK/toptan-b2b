package com.example.toptan.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class ToptanciViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance() // Storage bağlantısı eklendi

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj

    // Fonksiyona gorselUri parametresi eklendi
    fun urunEkle(urunAdi: String, fiyatStr: String, minAlimStr: String, stokStr: String, gorselUri: Uri?) {
        val fiyat = fiyatStr.toDoubleOrNull()
        val minAlim = minAlimStr.toIntOrNull()
        val stok = stokStr.toIntOrNull()

        if (urunAdi.isEmpty() || fiyat == null || minAlim == null || stok == null) {
            _mesaj.value = "Lütfen tüm alanları geçerli şekilde doldurun."
            return
        }

        val toptanciId = auth.currentUser?.uid
        if (toptanciId == null) {
            _mesaj.value = "Hata: Kullanıcı oturumu bulunamadı."
            return
        }

        // 1. EĞER GÖRSEL SEÇİLMİŞSE ÖNCE STORAGE'A YÜKLE
        if (gorselUri != null) {
            _mesaj.value = "Fotoğraf yükleniyor, lütfen bekleyin..."
            // Rastgele benzersiz bir isimle fotoğrafı Storage'a gönderiyoruz
            val dosyaYolu = storage.reference.child("urun_gorselleri/${UUID.randomUUID()}.jpg")

            dosyaYolu.putFile(gorselUri)
                .addOnSuccessListener {
                    // Yükleme başarılıysa fotoğrafın internet linkini (URL) al
                    dosyaYolu.downloadUrl.addOnSuccessListener { uri ->
                        firestoreKaydet(toptanciId, urunAdi, fiyat, minAlim, stok, uri.toString())
                    }
                }
                .addOnFailureListener {
                    _mesaj.value = "Fotoğraf yüklenirken hata oluştu."
                }
        } else {
            // 2. GÖRSEL SEÇİLMEMİŞSE FOTOĞRAFSIZ KAYDET
            firestoreKaydet(toptanciId, urunAdi, fiyat, minAlim, stok, "")
        }
    }

    // Firestore'a yazma işlemini ayıran yardımcı fonksiyon
    private fun firestoreKaydet(toptanciId: String, ad: String, fiyat: Double, minAlim: Int, stok: Int, gorselUrl: String) {
        val yeniUrunRef = firestore.collection("urunler").document()

        // Nesneye gorselUrl de eklendi
        val urun = hashMapOf(
            "id" to yeniUrunRef.id,
            "toptanciId" to toptanciId,
            "ad" to ad,
            "fiyat" to fiyat,
            "minAlimMiktari" to minAlim,
            "stok" to stok,
            "gorselUrl" to gorselUrl
        )

        yeniUrunRef.set(urun)
            .addOnSuccessListener {
                _mesaj.value = "Ürün başarıyla eklendi!"
            }
            .addOnFailureListener { hata ->
                _mesaj.value = "Ürün eklenirken bir hata oluştu: ${hata.message}"
            }
    }

    fun mesajiTemizle() {
        _mesaj.value = null
    }
}