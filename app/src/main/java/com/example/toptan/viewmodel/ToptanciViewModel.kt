package com.example.toptan.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.toptan.model.Urun
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ToptanciViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj.asStateFlow()

    private val _yukleniyor = MutableStateFlow(false)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor.asStateFlow()

    // İstatistik Değişkenleri
    private val _toplamUrunSayisi = MutableStateFlow(0)
    val toplamUrunSayisi: StateFlow<Int> = _toplamUrunSayisi

    private val _bekleyenSiparisSayisi = MutableStateFlow(0)
    val bekleyenSiparisSayisi: StateFlow<Int> = _bekleyenSiparisSayisi

    private val _toplamCiro = MutableStateFlow(0.0)
    val toplamCiro: StateFlow<Double> = _toplamCiro

    // Toptancının kendi ürünlerini tutan liste
    private val _toptanciUrunleri = MutableStateFlow<List<Urun>>(emptyList())
    val toptanciUrunleri: StateFlow<List<Urun>> = _toptanciUrunleri

    // EKRAN AÇILDIĞINDA OTOMATİK ÇALIŞACAK KISIM
    init {
        istatistikleriGetir()
        fcmTokenKaydet()
    }

    private fun fcmTokenKaydet() {
        val userId = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val data = hashMapOf("fcmToken" to token)

                // Firestore'da kullanicilar koleksiyonuna token'ı yazıyoruz
                firestore.collection("kullanicilar").document(userId)
                    .set(data, SetOptions.merge())
            }
        }
    }

    fun istatistikleriGetir() {
        val toptanciId = auth.currentUser?.uid ?: return

        firestore.collection("urunler").whereEqualTo("toptanciId", toptanciId)
            .addSnapshotListener { snapshot, _ ->
                _toplamUrunSayisi.value = snapshot?.size() ?: 0
            }

        firestore.collection("siparisler").whereEqualTo("toptanciId", toptanciId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    var bekleyen = 0
                    var ciro = 0.0
                    for (doc in snapshot.documents) {
                        val durum = doc.getString("durum") ?: ""
                        val tutar = doc.getDouble("toplamTutar") ?: 0.0
                        if (durum != "Teslim Edildi") bekleyen++ else ciro += tutar
                    }
                    _bekleyenSiparisSayisi.value = bekleyen
                    _toplamCiro.value = ciro
                }
            }
    }

    fun toptanciUrunleriniGetir() {
        val toptanciId = auth.currentUser?.uid ?: return

        firestore.collection("urunler")
            .whereEqualTo("toptanciId", toptanciId)
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) return@addSnapshotListener

                val urunListesi = snapshot.documents.mapNotNull { it.toObject(Urun::class.java) }
                _toptanciUrunleri.value = urunListesi
            }
    }

    fun urunSil(urunId: String) {
        firestore.collection("urunler").document(urunId).delete()
            .addOnSuccessListener {
                _mesaj.value = "Ürün başarıyla silindi."
            }
            .addOnFailureListener {
                _mesaj.value = "Silme işlemi başarısız oldu."
            }
    }

    fun fiyatGuncelle(urunId: String, yeniFiyat: Double) {
        firestore.collection("urunler").document(urunId).update("fiyat", yeniFiyat)
            .addOnSuccessListener {
                _mesaj.value = "Fiyat başarıyla güncellendi."
            }
            .addOnFailureListener {
                _mesaj.value = "Fiyat güncellenemedi."
            }
    }

    // Ürün Ekleme (Storage Entegrasyonlu)
    fun urunEkle(urunAdi: String, fiyatStr: String, minAlimStr: String, stokStr: String, kategori: String, gorselUri: Uri?) {
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

        _yukleniyor.value = true

        if (gorselUri != null) {
            _mesaj.value = "Fotoğraf yükleniyor, lütfen bekleyin..."
            val dosyaYolu = storage.reference.child("urun_gorselleri/${UUID.randomUUID()}.jpg")

            dosyaYolu.putFile(gorselUri)
                .addOnSuccessListener {
                    dosyaYolu.downloadUrl.addOnSuccessListener { uri ->
                        firestoreKaydet(toptanciId, urunAdi, fiyat, minAlim, stok, kategori, uri.toString())
                    }
                }
                .addOnFailureListener {
                    _yukleniyor.value = false
                    _mesaj.value = "Fotoğraf yüklenirken hata oluştu."
                }
        } else {
            firestoreKaydet(toptanciId, urunAdi, fiyat, minAlim, stok, kategori, "")
        }
    }

    private fun firestoreKaydet(toptanciId: String, ad: String, fiyat: Double, minAlim: Int, stok: Int, kategori: String, gorselUrl: String) {
        val yeniUrunRef = firestore.collection("urunler").document()
        val urun = hashMapOf(
            "id" to yeniUrunRef.id,
            "toptanciId" to toptanciId,
            "ad" to ad,
            "fiyat" to fiyat,
            "minAlimMiktari" to minAlim,
            "stok" to stok,
            "kategori" to kategori,
            "gorselUrl" to gorselUrl,
            "eklenmeTarihi" to System.currentTimeMillis()
        )

        yeniUrunRef.set(urun)
            .addOnSuccessListener {
                _yukleniyor.value = false
                _mesaj.value = "Ürün başarıyla eklendi!"
            }
            .addOnFailureListener { hata ->
                _yukleniyor.value = false
                _mesaj.value = "Ürün eklenirken bir hata oluştu: ${hata.message}"
            }
    }

    fun mesajiTemizle() {
        _mesaj.value = null
    }
}