package com.example.toptan.model

// Toptancıların (Satıcıların) bilgilerini tutan veri modeli
data class Toptanci(
    val id: String = "",
    val ad: String = "",
    val kategori: String = "",
    val minSiparisTutari: Double = 0.0,
    val ayniGunKargo: Boolean = false,
    val onayliMi: Boolean = false
)

// Toptancıların sattığı ürünlerin veri modeli (Firestore eşleşmesi için varsayılan değerler eklendi)
data class Urun(
    val id: String = "",
    val toptanciId: String = "",
    val ad: String = "",
    val fiyat: Double = 0.0,
    val minAlimMiktari: Int = 0,
    val stokMiktari: Int = 0,
    val eklenmeTarihi: Long = 0L // Firestore'a kaydederken eklediğimiz tarih
)

// Sepette tutulacak ürünlerin modeli
data class SepetOgesi(
    val urun: Urun = Urun(),
    var secilenMiktar: Int = 0
)

// Sipariş geçmişinde görünecek olan veri modeli
// Sipariş verilerinin Firestore ile eşleşebilmesi için güncellenmiş hali
data class Siparis(
    val siparisId: String = "",
    val musteriUid: String = "",
    val musteriEmail: String = "",
    val toptanciId: String = "",
    val siparisOzeti: String = "",
    val toplamTutar: Double = 0.0,
    val durum: String = "",
    val tarih: Long = 0L
)