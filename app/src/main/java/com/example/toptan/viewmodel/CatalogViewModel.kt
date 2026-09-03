package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Urun
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CatalogViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _urunler = MutableStateFlow<List<Urun>>(emptyList())
    val urunler: StateFlow<List<Urun>> = _urunler

    private val _yukleniyor = MutableStateFlow(true)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor

    // INIT BLOĞUNU KALDIRDIK. Fonksiyon çağrıldığında çalışacak.
    fun urunleriGetir(toptanciId: String) {
        _yukleniyor.value = true
        // SADECE tıklanan toptancıya ait ürünleri filtrele: .whereEqualTo("toptanciId", toptanciId)
        firestore.collection("urunler")
            .whereEqualTo("toptanciId", toptanciId)
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) {
                    _yukleniyor.value = false
                    return@addSnapshotListener
                }

                val liste = snapshot.documents.mapNotNull { it.toObject(Urun::class.java) }
                _urunler.value = liste
                _yukleniyor.value = false
            }
    }
}