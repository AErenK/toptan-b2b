const { onDocumentCreated, onDocumentUpdated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

if (!admin.apps.length) {
    admin.initializeApp();
}

// -------------------------------------------------------------------
// 1. YENİ SİPARİŞ BİLDİRİMİ (Müşteri Sipariş Verince Toptancıya Gider)
// -------------------------------------------------------------------
exports.yeniSiparisBildirimi = onDocumentCreated("siparisler/{siparisId}", async (event) => {
    const snap = event.data;
    if (!snap) return;

    const yeniSiparis = snap.data();
    const toptanciId = yeniSiparis.toptanciId;

    if (!toptanciId) return;

    const toptanciDoc = await admin.firestore().collection('kullanicilar').doc(toptanciId).get();
    if (!toptanciDoc.exists) return;

    const fcmToken = toptanciDoc.data().fcmToken;
    if (!fcmToken) {
        console.log("Toptancının FCM Token'ı yok.");
        return;
    }

    const mesaj = {
        notification: {
            title: "📦 Yeni Sipariş Geldi!",
            body: `${yeniSiparis.toplamTutar} ₺ tutarında yeni bir siparişiniz var.`
        },
        token: fcmToken
    };

    try {
        await admin.messaging().send(mesaj);
        console.log("Toptancıya bildirim gönderildi.");
    } catch (error) {
        console.error("Toptancıya bildirim hatası:", error);
    }
});

// -------------------------------------------------------------------
// 2. SİPARİŞ DURUM BİLDİRİMİ (Toptancı Durumu Değiştirince Müşteriye Gider)
// -------------------------------------------------------------------
exports.siparisDurumBildirimi = onDocumentUpdated("siparisler/{siparisId}", async (event) => {
    // Güncelleme öncesi ve sonrası verileri al
    const oncekiVeri = event.data.before.data();
    const yeniVeri = event.data.after.data();

    // Sadece "durum" alanı değiştiyse çalışsın (gereksiz bildirimleri önler)
    if (oncekiVeri.durum === yeniVeri.durum) {
        return;
    }

    // Müşterinin UID'sini al
    const musteriUid = yeniVeri.musteriUid;
    if (!musteriUid) return;

    // Müşterinin veritabanındaki Token'ını bul
    const musteriDoc = await admin.firestore().collection('kullanicilar').doc(musteriUid).get();
    if (!musteriDoc.exists) return;

    const fcmToken = musteriDoc.data().fcmToken;
    if (!fcmToken) {
        console.log("Müşterinin FCM Token'ı yok, bildirim gönderilemedi.");
        return;
    }

    // Duruma göre dinamik mesaj oluştur
    let baslik = "📦 Sipariş Durumu Güncellendi";
    let icerik = "Siparişinizle ilgili yeni bir gelişme var.";

    if (yeniVeri.durum === "Yola Çıktı") {
        baslik = "🚚 Siparişiniz Yola Çıktı!";
        icerik = `${yeniVeri.toplamTutar} ₺ tutarındaki siparişiniz kargoya verilmiştir.`;
    } else if (yeniVeri.durum === "Teslim Edildi") {
        baslik = "✅ Siparişiniz Teslim Edildi!";
        icerik = "Siparişiniz başarıyla teslim edilmiştir. Bizi tercih ettiğiniz için teşekkürler!";
    } else {
        // Hazırlanıyor vb. başka durumlar için bildirim atmak istemiyorsak işlemi durdur
        return;
    }

    const mesaj = {
        notification: {
            title: baslik,
            body: icerik
        },
        token: fcmToken
    };

    try {
        await admin.messaging().send(mesaj);
        console.log(`Müşteriye (${yeniVeri.durum}) bildirimi gönderildi.`);
    } catch (error) {
        console.error("Müşteriye bildirim hatası:", error);
    }
});