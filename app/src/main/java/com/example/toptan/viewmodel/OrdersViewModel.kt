package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Firestore'dan gelecek veriyi eşleyeceğimiz Taslak (Model)
data class Siparis(
    val siparisId: String = "",
    val siparisOzeti: String = "",
    val toplamTutar: Double = 0.0,
    val durum: String = "",
    val tarih: Long = 0L
) {
    // Milisaniye cinsinden gelen tarihi "31 Ağu 2026, 14:30" formatına çevirir
    val formatliTarih: String
        get() {
            if (tarih == 0L) return ""
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr", "TR"))
            return sdf.format(Date(tarih))
        }
}

class OrdersViewModel : ViewModel() {
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
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _yukleniyor.value = false
            return
        }

        // Firestore'un gerçek zamanlı dinleyicisi (Yeni sipariş anında ekrana düşer)
        firestore.collection("siparisler")
            .whereEqualTo("musteriUid", uid)
            .orderBy("tarih", Query.Direction.DESCENDING) // En yeni sipariş en üstte
            .addSnapshotListener { snapshot, hata ->
                if (hata != null) {
                    _yukleniyor.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Firestore dokümanlarını otomatik olarak Siparis sınıfına dönüştürür
                    val liste = snapshot.documents.mapNotNull { it.toObject(Siparis::class.java) }
                    _siparisler.value = liste
                }
                _yukleniyor.value = false
            }
    }
}