package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.example.toptan.model.SepetOgesi
import com.example.toptan.model.Urun
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartViewModel : ViewModel() {

    // Sepetteki ürünlerin listesi
    private val _sepet = MutableStateFlow<List<SepetOgesi>>(emptyList())
    val sepet: StateFlow<List<SepetOgesi>> = _sepet.asStateFlow()

    // Alt barda gösterilecek toplam fiyat
    private val _toplamTutar = MutableStateFlow(0.0)
    val toplamTutar: StateFlow<Double> = _toplamTutar.asStateFlow()

    init {
        ornekSepetDoldur()
    }

    private fun ornekSepetDoldur() {
        val urun1 = Urun(id = "U1", toptanciId = "1", ad = "Torku Küp Şeker (Koli)", fiyat = 250.0, minAlimMiktari = 50, stokMiktari = 500)
        val urun2 = Urun(id = "U2", toptanciId = "2", ad = "Doğuş Çay 1kg (Koli)", fiyat = 600.0, minAlimMiktari = 20, stokMiktari = 200)

        _sepet.value = listOf(
            SepetOgesi(urun = urun1, secilenMiktar = 50),
            SepetOgesi(urun = urun2, secilenMiktar = 20)
        )
        toplamHesapla()
    }

    // Artı butonuna basıldığında çalışacak
    fun miktarArtir(urunId: String) {
        _sepet.value = _sepet.value.map { oge ->
            if (oge.urun.id == urunId && oge.secilenMiktar < oge.urun.stokMiktari) {
                oge.copy(secilenMiktar = oge.secilenMiktar + 1)
            } else oge
        }
        toplamHesapla()
    }

    // Eksi butonuna basıldığında çalışacak (B2B kuralı: Min alım miktarının altına düşemez)
    fun miktarAzalt(urunId: String) {
        _sepet.value = _sepet.value.map { oge ->
            if (oge.urun.id == urunId && oge.secilenMiktar > oge.urun.minAlimMiktari) {
                oge.copy(secilenMiktar = oge.secilenMiktar - 1)
            } else oge
        }
        toplamHesapla()
    }

    private fun toplamHesapla() {
        _toplamTutar.value = _sepet.value.sumOf { it.urun.fiyat * it.secilenMiktar }
    }
}