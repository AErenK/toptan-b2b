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

    private val _siparisler = MutableStateFlow<List<Siparis>>(emptyList())
    val siparisler: StateFlow<List<Siparis>> = _siparisler

    private val _yukleniyor = MutableStateFlow(true)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor

    init {
        siparisleriGetir()
    }

    private fun siparisleriGetir() {
        val aktifMusteriEmail = auth.currentUser?.email ?: return

        // Sadece bu müşteriye (email'e) ait siparişleri gerçek zamanlı dinliyoruz
        firestore.collection("siparisler")
            .whereEqualTo("musteriEmail", aktifMusteriEmail)
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) {
                    _yukleniyor.value = false
                    return@addSnapshotListener
                }

                val liste = snapshot.documents
                    .mapNotNull { it.toObject(Siparis::class.java) }
                    .sortedByDescending { it.tarih } // En yeni sipariş en üstte görünsün

                _siparisler.value = liste
                _yukleniyor.value = false
            }
    }
}