package com.example.toptan.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.toptan.MainActivity // Kendi paket adına göre kontrol et
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class B2BFirebaseMessagingService : FirebaseMessagingService() {

    // Yeni bir bildirim geldiğinde tetiklenen fonksiyon
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Bildirimin başlığı ve içeriği
        val baslik = remoteMessage.notification?.title ?: "Yeni Bildirim"
        val icerik = remoteMessage.notification?.body ?: "Uygulamanızda yeni bir gelişme var."

        bildirimiGoster(baslik, icerik)
    }

    // Uygulama silinip yüklenirse veya token değişirse tetiklenir
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Burada gerekirse AuthViewModel'deki token güncelleme fonksiyonunu tekrar tetikleyebiliriz
        println("Yeni FCM Token: $token")
    }

    private fun bildirimiGoster(baslik: String, mesaj: String) {
        val channelId = "b2b_toptan_kanal"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 (Oreo) ve üzeri için kanal oluşturma zorunluluğu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "B2B Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Bildirime tıklanınca uygulamayı açması için Intent
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(baslik)
            .setContentText(mesaj)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Kendi uygulama ikonunu da koyabilirsin (ör. R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        manager.notify(Random.nextInt(), notification)
    }
}