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
    val urunler: StateFlow<List<Urun>> = _urunler.asStateFlow()

    private val _yukleniyor = MutableStateFlow(true)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor.asStateFlow()

    // --- FİLTRELEME STATE'LERİ ---
    private val _aramaMetni = MutableStateFlow("")
    val aramaMetni: StateFlow<String> = _aramaMetni.asStateFlow()

    private val _seciliKategori = MutableStateFlow("Tümü")
    val seciliKategori: StateFlow<String> = _seciliKategori.asStateFlow()

    // Dinamik olarak oluşturulacak kategori listesi
    private val _kategoriler = MutableStateFlow<List<String>>(listOf("Tümü"))
    val kategoriler: StateFlow<List<String>> = _kategoriler.asStateFlow()

    fun urunleriGetir(toptanciId: String) {
        _yukleniyor.value = true
        firestore.collection("urunler")
            .whereEqualTo("toptanciId", toptanciId)
            .whereEqualTo("aktifMi", true) // YENİ: Sadece yayında olan (aktif) ürünleri getir
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val liste = snapshot.documents.mapNotNull { it.toObject(Urun::class.java)?.copy(id = it.id) }
                    _urunler.value = liste

                    // Ürünlerin içinden kategorileri okuyup benzersiz (distinct) bir liste yapıyoruz
                    val dinamikKategoriler = mutableListOf("Tümü")
                    dinamikKategoriler.addAll(liste.map { it.kategori }.filter { it.isNotBlank() }.distinct().sorted())
                    _kategoriler.value = dinamikKategoriler

                    _yukleniyor.value = false
                }
            }
    }

    fun aramaMetniGuncelle(metin: String) {
        _aramaMetni.value = metin
    }

    fun kategoriSec(kategori: String) {
        _seciliKategori.value = kategori
    }
}