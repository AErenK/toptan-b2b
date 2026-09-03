package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Siparis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MusteriSiparisViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _gecmisSiparisler = MutableStateFlow<List<Siparis>>(emptyList())
    val gecmisSiparisler: StateFlow<List<Siparis>> = _gecmisSiparisler

    private val _yukleniyor = MutableStateFlow(true)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor

    init {
        siparisleriGetir()
    }

    private fun siparisleriGetir() {
        val aktifMusteriId = auth.currentUser?.uid ?: return

        // Sadece bu müşterinin verdiği siparişleri dinle
        firestore.collection("siparisler")
            .whereEqualTo("musteriUid", aktifMusteriId)
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) {
                    _yukleniyor.value = false
                    return@addSnapshotListener
                }

                // Tarihe göre sırala (En yeni en üstte)
                val liste = snapshot.documents
                    .mapNotNull { it.toObject(Siparis::class.java) }
                    .sortedByDescending { it.tarih }

                _gecmisSiparisler.value = liste
                _yukleniyor.value = false
            }
    }
}