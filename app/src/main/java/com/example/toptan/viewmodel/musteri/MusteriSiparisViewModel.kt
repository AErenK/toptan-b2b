package com.example.toptan.viewmodel.musteri

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
        val aktifMusteriUid = auth.currentUser?.uid ?: return

        firestore.collection("siparisler")
            .whereEqualTo("musteriUid", aktifMusteriUid)
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) {
                    _yukleniyor.value = false
                    return@addSnapshotListener
                }
                val liste = snapshot.documents
                    .mapNotNull { it.toObject(Siparis::class.java) }
                    .sortedByDescending { it.tarih }
                _siparisler.value = liste
                _yukleniyor.value = false
            }
    }
}