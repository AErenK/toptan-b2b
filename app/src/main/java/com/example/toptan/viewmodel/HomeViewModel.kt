package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Toptanci
import com.example.toptan.model.Urun // YENİ: Urun modelini import ettik
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // YENİ: Sıralama için gerekli
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _toptancilar = MutableStateFlow<List<Toptanci>>(emptyList())
    val toptancilar: StateFlow<List<Toptanci>> = _toptancilar

    // --- YENİ: Yeni gelen ürünler için state ---
    private val _yeniGelenler = MutableStateFlow<List<Urun>>(emptyList())
    val yeniGelenler: StateFlow<List<Urun>> = _yeniGelenler

    init {
        toptancilariGetir()
        yeniUrunleriGetir() // YENİ: Vitrin ürünlerini de başlatırken çağırıyoruz
    }

    private fun toptancilariGetir() {
        // "kullanicilar" koleksiyonuna gidip sadece rolü "toptanci" olanları çekiyoruz (Kendi orijinal kodun)
        firestore.collection("kullanicilar")
            .whereEqualTo("rol", "toptanci")
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val liste = snapshot.documents.map { belge ->
                    Toptanci(
                        id = belge.id,
                        ad = belge.getString("email") ?: "Bilinmeyen Toptancı",
                        kategori = "Toptan Gıda",
                        minSiparisTutari = 1000.0,
                        ayniGunKargo = true,
                        onayliMi = true
                    )
                }
                _toptancilar.value = liste
            }
    }

    // --- YENİ: En son eklenen ürünleri çeken fonksiyon ---
    private fun yeniUrunleriGetir() {
        firestore.collection("urunler")
            .orderBy("eklenmeTarihi", Query.Direction.DESCENDING)
            .limit(10) // En yeni 10 ürün
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) return@addSnapshotListener

                val liste = snapshot.documents.mapNotNull { it.toObject(Urun::class.java) }
                _yeniGelenler.value = liste
            }
    }
}