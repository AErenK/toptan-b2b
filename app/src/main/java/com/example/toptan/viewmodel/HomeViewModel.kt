package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toptan.model.Toptanci
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {

    private val _toptancilar = MutableStateFlow<List<Toptanci>>(emptyList())
    val toptancilar: StateFlow<List<Toptanci>> = _toptancilar.asStateFlow()

    // Firestore veritabanı referansı
    private val db = FirebaseFirestore.getInstance()

    init {
        toptancilariGetir()
    }

    fun toptancilariGetir() {
        viewModelScope.launch {
            try {
                // Firestore'daki "toptancilar" koleksiyonundan verileri çekiyoruz
                val snapshot = db.collection("toptancilar").get().await()

                val liste = snapshot.documents.map { doc ->
                    Toptanci(
                        id = doc.id,
                        ad = doc.getString("ad") ?: "",
                        kategori = doc.getString("kategori") ?: "",
                        minSiparisTutari = doc.getDouble("minSiparisTutari") ?: 0.0,
                        ayniGunKargo = doc.getBoolean("ayniGunKargo") ?: false,
                        onayliMi = doc.getBoolean("onayliMi") ?: false
                    )
                }

                _toptancilar.value = liste
            } catch (e: Exception) {
                // Bağlantı hatası olursa boş kalmasın diye loglayabilirsin
                e.printStackTrace()
            }
        }
    }
}