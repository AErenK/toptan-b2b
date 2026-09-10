package com.example.toptan.viewmodel.toptanci

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Musteri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// YENİ: iskontoOrani eklendi

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

    fun musterileriGetir() {
        _yukleniyor.value = true
        firestore.collection("kullanicilar")
            .whereEqualTo("rol", "musteri")
            .get()
            .addOnSuccessListener { snapshot ->
                val musteriListesi = snapshot.documents.map { doc ->
                    Musteri(
                        uid = doc.id,
                        sirketUnvani = doc.getString("sirketUnvani") ?: "Belirtilmemiş Şirket",
                        yetkiliKisi = doc.getString("yetkiliKisi") ?: "-",
                        telefon = doc.getString("telefon") ?: "-",
                        email = doc.getString("email") ?: "-",
                        vergiNo = doc.getString("vergiNo") ?: "-",
                        vergiDairesi = doc.getString("vergiDairesi") ?: "-",
                        cariLimit = doc.getDouble("cariLimit") ?: 50000.0,
                        iskontoOrani = doc.getDouble("iskontoOrani") ?: 0.0 // YENİ
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

    fun cariLimitGuncelle(musteriUid: String, yeniLimit: Double) {
        firestore.collection("kullanicilar").document(musteriUid)
            .update("cariLimit", yeniLimit)
            .addOnSuccessListener {
                _mesaj.value = "Cari limit başarıyla güncellendi! (başarılı)"
                musterileriGetir()
            }
            .addOnFailureListener { _mesaj.value = "Limit güncellenemedi." }
    }

    // YENİ: İskonto Güncelleme Motoru
    fun iskontoGuncelle(musteriUid: String, yeniIskonto: Double) {
        firestore.collection("kullanicilar").document(musteriUid)
            .update("iskontoOrani", yeniIskonto)
            .addOnSuccessListener {
                _mesaj.value = "Müşteriye özel %$yeniIskonto iskonto tanımlandı! (başarılı)"
                musterileriGetir()
            }
            .addOnFailureListener { _mesaj.value = "İskonto tanımlanamadı." }
    }

    fun tahsilatEkle(musteriUid: String, tutar: Double, aciklama: String) {
        val toptanciId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val batch = firestore.batch()

        val musteriRef = firestore.collection("kullanicilar").document(musteriUid)
        batch.update(musteriRef, "cariLimit", FieldValue.increment(tutar))

        val hareketRef = firestore.collection("cari_hareketler").document()
        val makbuz = hashMapOf(
            "islemId" to hareketRef.id,
            "musteriUid" to musteriUid,
            "toptanciId" to toptanciId,
            "islemTuru" to "Tahsilat",
            "tutar" to tutar,
            "aciklama" to aciklama,
            "tarih" to System.currentTimeMillis()
        )
        batch.set(hareketRef, makbuz)

        batch.commit()
            .addOnSuccessListener {
                _mesaj.value = "Tahsilat başarıyla işlendi ve müşterinin limitine eklendi. (başarılı)"
                musterileriGetir()
            }
            .addOnFailureListener { _mesaj.value = "Tahsilat işlemi başarısız oldu." }
    }

    fun mesajiTemizle() { _mesaj.value = null }
}