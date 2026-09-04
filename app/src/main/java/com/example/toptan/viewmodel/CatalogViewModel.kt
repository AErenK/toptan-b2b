package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Urun
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CatalogViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _urunler = MutableStateFlow<List<Urun>>(emptyList())
    val urunler: StateFlow<List<Urun>> = _urunler

    private val _yukleniyor = MutableStateFlow(true)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor

    // YENİ: Arama metnini tutacak state
    private val _aramaMetni = MutableStateFlow("")
    val aramaMetni: StateFlow<String> = _aramaMetni.asStateFlow()

    fun urunleriGetir(toptanciId: String) {
        _yukleniyor.value = true
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

    // YENİ: Arama metnini güncelleyen fonksiyon
    fun aramaMetniniGuncelle(yeniMetin: String) {
        _aramaMetni.value = yeniMetin
    }
}