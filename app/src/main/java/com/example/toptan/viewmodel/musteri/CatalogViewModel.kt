package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Urun
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CatalogViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance() // YENİ: Oturum kontrolü için eklendi

    private val _urunler = MutableStateFlow<List<Urun>>(emptyList())
    val urunler: StateFlow<List<Urun>> = _urunler.asStateFlow()

    private val _yukleniyor = MutableStateFlow(true)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor.asStateFlow()

    private val _aramaMetni = MutableStateFlow("")
    val aramaMetni: StateFlow<String> = _aramaMetni.asStateFlow()

    private val _seciliKategori = MutableStateFlow("Tümü")
    val seciliKategori: StateFlow<String> = _seciliKategori.asStateFlow()

    private val _kategoriler = MutableStateFlow<List<String>>(listOf("Tümü", "Favorilerim"))
    val kategoriler: StateFlow<List<String>> = _kategoriler.asStateFlow()

    // YENİ: Müşterinin beğendiği ürünlerin ID'lerini tutan liste
    private val _favoriUrunIds = MutableStateFlow<List<String>>(emptyList())
    val favoriUrunIds: StateFlow<List<String>> = _favoriUrunIds.asStateFlow()

    init {
        favorileriDinle()
    }

    // YENİ: Firebase'den müşterinin favori listesini anlık olarak çeker
    private fun favorileriDinle() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("kullanicilar").document(uid).addSnapshotListener { snap, _ ->
            if (snap != null && snap.exists()) {
                val favoriler = snap.get("favoriUrunler") as? List<String> ?: emptyList()
                _favoriUrunIds.value = favoriler
            }
        }
    }

    // YENİ: Ürün zaten favoriyse çıkarır, değilse ekler
    fun favoriDurumuDegistir(urunId: String) {
        val uid = auth.currentUser?.uid ?: return
        val userRef = firestore.collection("kullanicilar").document(uid)

        if (_favoriUrunIds.value.contains(urunId)) {
            userRef.update("favoriUrunler", FieldValue.arrayRemove(urunId))
        } else {
            userRef.update("favoriUrunler", FieldValue.arrayUnion(urunId))
        }
    }

    fun urunleriGetir(toptanciId: String) {
        _yukleniyor.value = true
        firestore.collection("urunler")
            .whereEqualTo("toptanciId", toptanciId)
            .whereEqualTo("aktifMi", true)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val liste = snapshot.documents.mapNotNull { it.toObject(Urun::class.java)?.copy(id = it.id) }
                    _urunler.value = liste

                    // YENİ: Kategorilerin en başına "Tümü" ve "Favorilerim" eklendi
                    val dinamikKategoriler = mutableListOf("Tümü", "Favorilerim")
                    dinamikKategoriler.addAll(liste.map { it.kategori }.filter { it.isNotBlank() }.distinct().sorted())
                    _kategoriler.value = dinamikKategoriler

                    _yukleniyor.value = false
                }
            }
    }

    fun aramaMetniGuncelle(metin: String) { _aramaMetni.value = metin }
    fun kategoriSec(kategori: String) { _seciliKategori.value = kategori }
}