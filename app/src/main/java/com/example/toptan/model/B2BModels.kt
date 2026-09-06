package com.example.toptan.model

data class Toptanci(
    val id: String = "",
    val ad: String = "",
    val kategori: String = "",
    val minSiparisTutari: Double = 0.0,
    val ayniGunKargo: Boolean = false,
    val onayliMi: Boolean = false
)

data class Urun(
    val id: String = "",
    val toptanciId: String = "",
    val ad: String = "",
    val fiyat: Double = 0.0,
    val minAlimMiktari: Int = 0,
    val stokMiktari: Int = 0,
    val gorselUrl: String = "",
    val stok: Int = 0,
    val eklenmeTarihi: Long = 0L,
    val kategori: String = "Diğer"
)

data class SepetOgesi(
    val urun: Urun = Urun(),
    var secilenMiktar: Int = 0
)

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

data class Kullanici(
    val uid: String = "",
    val ad: String = "",
    val eposta: String = "",
    val rol: String = "", // "Musteri" veya "Toptanci"
    val fcmToken: String = "" // BİLDİRİMLER İÇİN GEREKLİ ALAN
)