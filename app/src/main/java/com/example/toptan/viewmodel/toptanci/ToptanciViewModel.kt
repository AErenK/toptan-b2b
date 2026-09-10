package com.example.toptan.viewmodel.toptanci

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.toptan.model.Siparis
import com.example.toptan.model.Urun
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class ToptanciViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj.asStateFlow()

    private val _yukleniyor = MutableStateFlow(false)
    val yukleniyor: StateFlow<Boolean> = _yukleniyor.asStateFlow()

    // İstatistik Değişkenleri
    private val _toplamUrunSayisi = MutableStateFlow(0)
    val toplamUrunSayisi: StateFlow<Int> = _toplamUrunSayisi

    private val _bekleyenSiparisSayisi = MutableStateFlow(0)
    val bekleyenSiparisSayisi: StateFlow<Int> = _bekleyenSiparisSayisi

    private val _toplamCiro = MutableStateFlow(0.0)
    val toplamCiro: StateFlow<Double> = _toplamCiro

    // Toptancının kendi ürünlerini tutan liste
    private val _toptanciUrunleri = MutableStateFlow<List<Urun>>(emptyList())
    val toptanciUrunleri: StateFlow<List<Urun>> = _toptanciUrunleri
    private val _toptanciSiparisleri = MutableStateFlow<List<Siparis>>(emptyList())
    val toptanciSiparisleri: StateFlow<List<Siparis>> = _toptanciSiparisleri

    private val _siparislerYukleniyor = MutableStateFlow(true)
    val siparislerYukleniyor: StateFlow<Boolean> = _siparislerYukleniyor

    private val _minSiparisTutari = MutableStateFlow(0.0)
    val minSiparisTutari: StateFlow<Double> = _minSiparisTutari.asStateFlow()

    private val _enCokSatanUrunler = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val enCokSatanUrunler: StateFlow<List<Pair<String, Int>>> = _enCokSatanUrunler.asStateFlow()

    private val _enIyiMusteriler = MutableStateFlow<List<Pair<String, Double>>>(emptyList())
    val enIyiMusteriler: StateFlow<List<Pair<String, Double>>> = _enIyiMusteriler.asStateFlow()

    init {
        istatistikleriGetir()
        toptanciUrunleriniGetir()
        siparisleriGetir()
        toptanciAyarlariniGetir()
    }

    private fun fcmTokenKaydet() {
        val userId = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                val data = hashMapOf("fcmToken" to token)

                // Firestore'da kullanicilar koleksiyonuna token'ı yazıyoruz
                firestore.collection("kullanicilar").document(userId)
                    .set(data, SetOptions.merge())
            }
        }
    }

    fun siparisleriGetir() {
        val toptanciId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        _siparislerYukleniyor.value = true

        FirebaseFirestore.getInstance().collection("siparisler")
            .whereEqualTo("toptanciId", toptanciId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val liste = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Siparis::class.java)?.copy(siparisId = doc.id)
                    }
                    // En yeni siparişler en üstte görünsün diye tarihe göre sıralıyoruz
                    _toptanciSiparisleri.value = liste.sortedByDescending { it.tarih }
                    analizleriHesapla(liste)
                    _siparislerYukleniyor.value = false
                }
            }
    }

    fun siparisDurumuGuncelle(siparisId: String, yeniDurum: String) {
        FirebaseFirestore.getInstance().collection("siparisler").document(siparisId)
            .update("durum", yeniDurum)
    }

    fun siparisIptalEtVeIadeYap(siparis: Siparis) {
        val batch = firestore.batch()

        // 1. Siparişin durumunu "İptal Edildi" yap
        val siparisRef = firestore.collection("siparisler").document(siparis.siparisId)
        batch.update(siparisRef, "durum", "İptal Edildi")

        // 2. Müşterinin cari limitine (kredisine) sipariş tutarını geri ekle (İADE)
        val musteriRef = firestore.collection("kullanicilar").document(siparis.musteriUid)
        batch.update(musteriRef, "cariLimit", FieldValue.increment(siparis.toplamTutar))

        // 3. İşlemleri ateşle (İkisi aynı anda hatasız gerçekleşmek zorunda)
        batch.commit()
            .addOnSuccessListener {
                _mesaj.value = "Sipariş iptal edildi ve ${siparis.toplamTutar} ₺ müşteriye iade edildi."
            }
            .addOnFailureListener { hata ->
                _mesaj.value = "İptal işlemi başarısız: ${hata.message}"
            }
    }

    fun istatistikleriGetir() {
        val toptanciId = auth.currentUser?.uid ?: return

        firestore.collection("urunler").whereEqualTo("toptanciId", toptanciId)
            .addSnapshotListener { snapshot, _ ->
                _toplamUrunSayisi.value = snapshot?.size() ?: 0
            }

        firestore.collection("siparisler").whereEqualTo("toptanciId", toptanciId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    var bekleyen = 0
                    var ciro = 0.0
                    for (doc in snapshot.documents) {
                        val durum = doc.getString("durum") ?: ""
                        val tutar = doc.getDouble("toplamTutar") ?: 0.0
                        if (durum != "Teslim Edildi") bekleyen++ else ciro += tutar
                    }
                    _bekleyenSiparisSayisi.value = bekleyen
                    _toplamCiro.value = ciro
                }
            }
    }

    fun toptanciUrunleriniGetir() {
        val toptanciId = auth.currentUser?.uid ?: return

        firestore.collection("urunler")
            .whereEqualTo("toptanciId", toptanciId)
            .addSnapshotListener { snapshot, hata ->
                if (hata != null || snapshot == null) return@addSnapshotListener

                val urunListesi = snapshot.documents.mapNotNull { it.toObject(Urun::class.java) }
                _toptanciUrunleri.value = urunListesi
            }
    }

    fun urunSil(urunId: String) {
        firestore.collection("urunler").document(urunId).delete()
            .addOnSuccessListener {
                _mesaj.value = "Ürün başarıyla silindi."
            }
            .addOnFailureListener {
                _mesaj.value = "Silme işlemi başarısız oldu."
            }
    }

    // --- YENİ EKLENEN GÜNCELLEME FONKSİYONLARI ---

    // Hem fiyatı hem de stoğu aynı anda günceller
    fun urunGuncelle(urunId: String, yeniFiyat: Double, yeniStok: Int) {
        val guncelVeriler = mapOf(
            "fiyat" to yeniFiyat,
            "stok" to yeniStok
        )

        firestore.collection("urunler").document(urunId).update(guncelVeriler)
            .addOnSuccessListener {
                _mesaj.value = "Ürün bilgileri başarıyla güncellendi."
            }
            .addOnFailureListener {
                _mesaj.value = "Güncelleme başarısız oldu."
            }
    }

    // Ürünü satışa açar veya kapatır (Aktif / Pasif)
    fun urunAktiflikDegistir(urunId: String, aktifMi: Boolean) {
        firestore.collection("urunler").document(urunId).update("aktifMi", aktifMi)
            .addOnSuccessListener {
                val durum = if (aktifMi) "satışa açıldı" else "pasife alındı"
                _mesaj.value = "Ürün başarıyla $durum."
            }
    }

    // Ürün Ekleme (Storage Entegrasyonlu)
    fun urunEkle(urunAdi: String, fiyatStr: String, minAlimStr: String, stokStr: String, kategori: String, gorselUri: Uri?) {
        val fiyat = fiyatStr.toDoubleOrNull()
        val minAlim = minAlimStr.toIntOrNull()
        val stok = stokStr.toIntOrNull()

        if (urunAdi.isEmpty() || fiyat == null || minAlim == null || stok == null) {
            _mesaj.value = "Lütfen tüm alanları geçerli şekilde doldurun."
            return
        }

        val toptanciId = auth.currentUser?.uid
        if (toptanciId == null) {
            _mesaj.value = "Hata: Kullanıcı oturumu bulunamadı."
            return
        }

        _yukleniyor.value = true

        if (gorselUri != null) {
            _mesaj.value = "Fotoğraf yükleniyor, lütfen bekleyin..."
            val dosyaYolu = storage.reference.child("urun_gorselleri/${UUID.randomUUID()}.jpg")

            dosyaYolu.putFile(gorselUri)
                .addOnSuccessListener {
                    dosyaYolu.downloadUrl.addOnSuccessListener { uri ->
                        firestoreKaydet(toptanciId, urunAdi, fiyat, minAlim, stok, kategori, uri.toString())
                    }
                }
                .addOnFailureListener {
                    _yukleniyor.value = false
                    _mesaj.value = "Fotoğraf yüklenirken hata oluştu."
                }
        } else {
            firestoreKaydet(toptanciId, urunAdi, fiyat, minAlim, stok, kategori, "")
        }
    }

    private fun firestoreKaydet(toptanciId: String, ad: String, fiyat: Double, minAlim: Int, stok: Int, kategori: String, gorselUrl: String) {
        val yeniUrunRef = firestore.collection("urunler").document()
        val urun = hashMapOf(
            "id" to yeniUrunRef.id,
            "toptanciId" to toptanciId,
            "ad" to ad,
            "fiyat" to fiyat,
            "minAlimMiktari" to minAlim,
            "stok" to stok,
            "kategori" to kategori,
            "gorselUrl" to gorselUrl,
            "eklenmeTarihi" to System.currentTimeMillis()
        )

        yeniUrunRef.set(urun)
            .addOnSuccessListener {
                _yukleniyor.value = false
                _mesaj.value = "Ürün başarıyla eklendi!"
            }
            .addOnFailureListener { hata ->
                _yukleniyor.value = false
                _mesaj.value = "Ürün eklenirken bir hata oluştu: ${hata.message}"
            }
    }

    fun toptanciAyarlariniGetir() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("kullanicilar").document(uid).addSnapshotListener { snap, _ ->
            if (snap != null && snap.exists()) {
                _minSiparisTutari.value = snap.getDouble("minSiparisTutari") ?: 0.0
            }
        }
    }

    fun minSiparisTutariniGuncelle(yeniTutar: Double) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("kullanicilar").document(uid).update("minSiparisTutari", yeniTutar)
            .addOnSuccessListener { _mesaj.value = "Minimum sipariş tutarı güncellendi." }
    }

    // Bu fonksiyonu siparisleriGetir() içindeki SnapshotListener başarılı olduğunda çağır (liste değiştikçe çalışsın)
    fun analizleriHesapla(siparisler: List<Siparis>) {
        val gecerliSiparisler = siparisler.filter { it.durum != "İptal Edildi" }

        // 1. En İyi Müşteriler (Ciroya Göre)
        val musteriCiroMap = gecerliSiparisler.groupBy { it.sirketUnvani }
            .mapValues { entry -> entry.value.sumOf { it.toplamTutar } }
        _enIyiMusteriler.value = musteriCiroMap.toList().sortedByDescending { it.second }.take(3)

        // 2. En Çok Satan Ürünler (siparisOzeti parse ediliyor)
        val urunSatisMap = mutableMapOf<String, Int>()
        val regex = Regex("(\\d+)x\\s(.*?)(?:,|$)")

        gecerliSiparisler.forEach { siparis ->
            val matches = regex.findAll(siparis.siparisOzeti)
            matches.forEach { match ->
                val adet = match.groupValues[1].toIntOrNull() ?: 0
                val urunAdi = match.groupValues[2].trim()
                urunSatisMap[urunAdi] = urunSatisMap.getOrDefault(urunAdi, 0) + adet
            }
        }
        _enCokSatanUrunler.value = urunSatisMap.toList().sortedByDescending { it.second }.take(3)
    }

    fun mesajiTemizle() {
        _mesaj.value = null
    }
}