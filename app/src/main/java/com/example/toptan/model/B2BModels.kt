package com.example.toptan.model

// Toptancıların (Satıcıların) bilgilerini tutan veri modeli
data class Toptanci(
    val id: String,
    val ad: String,
    val kategori: String,
    val minSiparisTutari: Double,
    val ayniGunKargo: Boolean = false,
    val onayliMi: Boolean = false
)

// Toptancıların sattığı ürünlerin veri modeli
data class Urun(
    val id: String,
    val toptanciId: String, // Hangi toptancıya ait olduğunu bilmek için
    val ad: String,
    val fiyat: Double,
    val minAlimMiktari: Int, // Perakende değil toptan olduğu için en az alınabilecek miktar (Örn: 50)
    val stokMiktari: Int
)

// Sepette tutulacak ürünlerin modeli
data class SepetOgesi(
    val urun: Urun,
    var secilenMiktar: Int // Kullanıcının artırıp azaltacağı miktar
)

// Sipariş geçmişinde görünecek olan veri modeli
data class Siparis(
    val siparisNo: String,
    val toptanciAd: String,
    val toplamTutar: Double,
    val durum: String // "Hazırlanıyor", "Yola Çıktı", vb.
)