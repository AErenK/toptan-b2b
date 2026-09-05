package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.Urun
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ToptanciUrunEkleViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    var urunAdi = MutableStateFlow("")
    var fiyat = MutableStateFlow("")
    var minAlim = MutableStateFlow("")
    var gorselUrl = MutableStateFlow("")

    private val _yukleniyor = MutableStateFlow(false)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor.asStateFlow()

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj.asStateFlow()

    fun urunEkle(toptanciId: String, onSuccess: () -> Unit) {
        val adInput = urunAdi.value.trim()
        val fiyatInput = fiyat.value.toDoubleOrNull()
        val minAlimInput = minAlim.value.toIntOrNull()
        val urlInput = gorselUrl.value.trim()

        if (adInput.isEmpty() || fiyatInput == null || minAlimInput == null) {
            _mesaj.value = "Lütfen ürün adı, geçerli bir fiyat ve minimum alım miktarını eksiksiz girin."
            return
        }

        _yukleniyor.value = true
        val yeniUrunId = UUID.randomUUID().toString()

        val yeniUrun = Urun(
            id = yeniUrunId,
            toptanciId = toptanciId,
            ad = adInput,
            fiyat = fiyatInput,
            minAlimMiktari = minAlimInput,
            gorselUrl = urlInput
        )

        firestore.collection("urunler").document(yeniUrunId).set(yeniUrun)
            .addOnSuccessListener {
                _yukleniyor.value = false
                _mesaj.value = "Ürün başarıyla eklendi!"
                // Formu temizle
                urunAdi.value = ""
                fiyat.value = ""
                minAlim.value = ""
                gorselUrl.value = ""
                onSuccess()
            }
            .addOnFailureListener {
                _yukleniyor.value = false
                _mesaj.value = "Ürün eklenirken bir hata oluştu: ${it.message}"
            }
    }

    fun mesajTuketildi() {
        _mesaj.value = null
    }
}