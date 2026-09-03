package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Siparis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ToptanciSiparisViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _gelenSiparisler = MutableStateFlow<List<Siparis>>(emptyList())
    val gelenSiparisler: StateFlow<List<Siparis>> = _gelenSiparisler

    private val _yukleniyor = MutableStateFlow(true)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor

    init {
        siparisleriGetir()
    }

    private fun siparisleriGetir() {
        val aktifToptanciId = auth.currentUser?.uid ?: return

        firestore.collection("siparisler")
            .whereEqualTo("toptanciId", aktifToptanciId)
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) {
                    _yukleniyor.value = false
                    return@addSnapshotListener
                }

                val liste = snapshot.documents
                    .mapNotNull { it.toObject(Siparis::class.java) }
                    .sortedByDescending { it.tarih }

                _gelenSiparisler.value = liste
                _yukleniyor.value = false
            }
    }

    // YENİ EKLENEN FONKSİYON: Sipariş durumunu günceller
    fun siparisDurumuGuncelle(siparisId: String, yeniDurum: String) {
        if (siparisId.isEmpty()) return

        firestore.collection("siparisler").document(siparisId)
            .update("durum", yeniDurum)
            .addOnSuccessListener {
                // Başarılı olduğunda SnapshotListener otomatik olarak ekranı güncelleyecek
            }
    }
}