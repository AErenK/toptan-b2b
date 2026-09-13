package com.example.toptan.viewmodel.musteri

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Toptanci
import com.example.toptan.model.Urun
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val _toptancilar = MutableStateFlow<List<Toptanci>>(emptyList())
    val toptancilar: StateFlow<List<Toptanci>> = _toptancilar

    private val _yeniGelenler = MutableStateFlow<List<Urun>>(emptyList())
    val yeniGelenler: StateFlow<List<Urun>> = _yeniGelenler

    init {
        toptancilariGetir()
        yeniUrunleriGetir()
    }

    private fun toptancilariGetir() {
        firestore.collection("kullanicilar")
            .whereEqualTo("rol", "toptanci")
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) return@addSnapshotListener

                val liste = snapshot.documents.map { belge ->
                    Toptanci(
                        id = belge.id,
                        ad = belge.getString("sirketUnvani") ?: "Belirtilmemiş Şirket", // YENİ: Gerçek şirket adı
                        kategori = "Genel Toptan",
                        minSiparisTutari = belge.getDouble("minSiparisTutari") ?: 0.0, // YENİ: Gerçek limit
                        ayniGunKargo = true,
                        onayliMi = true
                    )
                }
                _toptancilar.value = liste
            }
    }

    private fun yeniUrunleriGetir() {
        firestore.collection("urunler")
            .whereEqualTo("aktifMi", true) // YENİ: Sadece satışta olanları göster
            .orderBy("eklenmeTarihi", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) return@addSnapshotListener
                val liste = snapshot.documents.mapNotNull { it.toObject(Urun::class.java) }
                _yeniGelenler.value = liste
            }
    }
}