package com.example.toptan.viewmodel.musteri

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Urun
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductDetailViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _urun = MutableStateFlow<Urun?>(null)
    val urun: StateFlow<Urun?> = _urun

    private val _yukleniyor = MutableStateFlow(true)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor

    fun urunuGetir(urunId: String) {
        _yukleniyor.value = true
        firestore.collection("urunler").document(urunId).get()
            .addOnSuccessListener { belge ->
                if (belge.exists()) {
                    _urun.value = belge.toObject(Urun::class.java)
                }
                _yukleniyor.value = false
            }
            .addOnFailureListener {
                _yukleniyor.value = false
            }
    }
}