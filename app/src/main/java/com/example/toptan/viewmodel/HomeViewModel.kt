package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Toptanci
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _toptancilar = MutableStateFlow<List<Toptanci>>(emptyList())
    val toptancilar: StateFlow<List<Toptanci>> = _toptancilar

    init {
        toptancilariGetir()
    }

    private fun toptancilariGetir() {
        // "kullanicilar" koleksiyonuna gidip sadece rolü "toptanci" olanları çekiyoruz
        firestore.collection("kullanicilar")
            .whereEqualTo("rol", "toptanci")
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val liste = snapshot.documents.map { belge ->
                    Toptanci(
                        id = belge.id, // Toptancının Firebase UID'si (Kritik nokta)
                        ad = belge.getString("email") ?: "Bilinmeyen Toptancı", // Şimdilik mağaza adı olarak e-postasını gösteriyoruz
                        kategori = "Toptan Gıda",
                        minSiparisTutari = 1000.0, // Şimdilik varsayılan bir tutar
                        ayniGunKargo = true,
                        onayliMi = true
                    )
                }
                _toptancilar.value = liste
            }
    }
}