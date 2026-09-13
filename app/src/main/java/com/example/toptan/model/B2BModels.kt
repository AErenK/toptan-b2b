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
    val stok: Int = 0,
    val kategori: String = "",
    val gorselUrl: String = "",
    val eklenmeTarihi: Long = 0L,
    val aktifMi: Boolean = true
)

data class SepetOgesi(
    val urun: Urun = Urun(),
    var secilenMiktar: Int = 0
)

data class Siparis(
    val siparisId: String = "",
    val musteriUid: String = "",
    val musteriEmail: String = "",
    val sirketUnvani: String = "",
    val teslimatAdresi: String = "",
    val vergiNo: String = "",
    val vergiDairesi: String = "",

    val yetkiliKisi: String = "",
    val telefon: String = "",

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
    val rol: String = "",
    val fcmToken: String = ""
)

data class Musteri(
    val uid: String,
    val sirketUnvani: String,
    val yetkiliKisi: String,
    val telefon: String,
    val email: String,
    val vergiNo: String,
    val vergiDairesi: String,
    val cariLimit: Double,
    val iskontoOrani: Double = 0.0
)

data class CariHareket(
    val islemId: String = "",
    val musteriUid: String = "",
    val toptanciId: String = "",
    val islemTuru: String = "",
    val tutar: Double = 0.0,
    val aciklama: String = "",
    val tarih: Long = 0L
)