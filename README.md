# 🚀 Toptan B2B - Premium E-Ticaret ve Sipariş Yönetim Sistemi

Modern, güvenli ve yüksek performanslı bir B2B (İşletmeden İşletmeye) toptan ticaret platformu. Kotlin, Jetpack Compose ve Firebase kullanılarak MVVM mimarisiyle sıfırdan geliştirilmiştir. Toptancılar ve perakendeciler (müşteriler) arasındaki ticaret, stok takibi ve cari hesap yönetimini tamamen dijitalleştirir.

## 🌟 Öne Çıkan Özellikler

Sistem, iki farklı kullanıcı yetkisine (Toptancı ve Müşteri) göre tamamen farklı arayüzler ve yetkiler sunar.

### 🏢 Toptancı (Yönetici) Modülü
* **Gelişmiş Dashboard:** Anlık toplam ciro, bekleyen siparişler, en çok satan ürünler ve en iyi müşteriler (satış analizleri).
* **Toplu Ürün Yükleme (CSV):** Tek tuşla Excel/CSV dosyasından binlerce ürünü saniyeler içinde kataloğa aktarma.
* **Cari ve Müşteri Yönetimi:** Müşterilere özel kredi limiti (açık hesap) tanımlama, özel VIP iskonto (% indirim) uygulama ve nakit/havale tahsilatı işleme.
* **Akıllı Sipariş Yönetimi:** Sipariş durumlarını "Hazırlanıyor", "Kargoya Verildi", "Teslim Edildi" olarak güncelleme ve tek tuşla sipariş iptali/bakiye iadesi yapma.

### 🛒 Müşteri (Perakendeci) Modülü
* **Dinamik Katalog ve Favoriler:** Satışta olan ürünleri arama, kategorileme ve sık alınan ürünleri "Favorilerim" listesine ekleyerek hızlı alışveriş yapma.
* **Akıllı B2B Sepeti:** Toptancının belirlediği "Minimum Sepet Tutarı"nı gösteren canlı ilerleme çubuğu (Progress Bar) ve anlık stok/limit kontrolleri.
* **Tekrarla (Re-order) Butonu:** Geçmiş siparişlerdeki "Tekrarla" butonuna basarak, eski siparişi güncel stok ve fiyatlarla anında yeni sepete kopyalama algoritması.
* **PDF Fatura ve Görsel Kargo Takibi:** Geçmiş siparişlerin durumunu görsel zaman çizelgesinde (Timeline) takip etme ve resmi sipariş fişlerini PDF olarak cihaza indirme.
* **Cari Kart:** Kullanılabilir açık hesap limitini ve harcama oranını gösteren platin/gold üyelik arayüzü.

### 🎨 Tasarım ve Deneyim (UI/UX)
* **Kusursuz Karanlık Tema (Dark Mode):** Cihazın temasına anında tepki veren, göz yormayan şık "Slate" renk paleti ve dinamik System/Navigation Bar entegrasyonu.
* **Jetpack Compose:** Tamamen modern, deklaratif ve animasyonlu kullanıcı arayüzü bileşenleri.

---

## 📱 Ekran Görüntüleri

| Müşteri Ana Ekran | Toptancı Dashboard | Ürün Kataloğu | Sipariş Yönetimi | Müşteri Profili ve Cari |
| :---: | :---: | :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/121f7e56-9c8d-4db2-ad9a-79baa608b918" width="200"/> | <img src="https://github.com/user-attachments/assets/4bed1861-f753-4268-885e-d9477a7f8c57" width="200"/> | <img src="https://github.com/user-attachments/assets/8f90f504-8d09-4b40-a4df-c3ece6d45e84" width="200"/> | <img src="https://github.com/user-attachments/assets/e3e8f5e9-c463-4b3e-b281-a769bc28111c" width="200"/> | <img src="https://github.com/user-attachments/assets/b414cbf1-492d-45a8-9ef8-2e35150d3c8c" width="200"/> |

---

## 🛠 Kullanılan Teknolojiler (Tech Stack)

* **Dil:** Kotlin
* **Arayüz (UI):** Jetpack Compose, Material Design 3
* **Mimari:** MVVM (Model-View-ViewModel), Single-Activity Architecture
* **Asenkron İşlemler:** Kotlin Coroutines & StateFlow
* **Veritabanı:** Firebase Firestore (NoSQL, Gerçek zamanlı dinleme)
* **Kimlik Doğrulama:** Firebase Authentication
* **Medya Yönetimi:** Firebase Storage & Coil (Görsel önbellekleme)
* **Bildirimler:** Firebase Cloud Messaging (FCM)
* **Ekstra Entegrasyonlar:** PDFDocument (Cihaz içi fatura oluşturma)

---

## 🔒 Güvenlik (Security Rules)

Veritabanı dışarıdan gelecek müdahalelere karşı tamamen kilitlenmiştir. Firestore Security Rules ile:
- Müşteriler yalnızca kendi siparişlerini ve cari makbuzlarını okuyabilir.
- Yalnızca "toptancı" rolüne sahip kullanıcılar ürün ekleyebilir/silebilir ve tahsilat makbuzu kesebilir.

---

## ⚙️ Kurulum ve Çalıştırma

1. Projeyi bilgisayarınıza klonlayın:
   ```bash
   git clone [https://github.com/KULLANICI_ADINIZ/toptan.git](https://github.com/KULLANICI_ADINIZ/toptan.git)
