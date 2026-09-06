package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.messaging.FirebaseMessaging

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj

    private val _kullaniciRolu = MutableStateFlow<String?>(null)
    val kullaniciRolu: StateFlow<String?> = _kullaniciRolu

    init {
        auth.currentUser?.let { kullanici ->
            roluGetir(kullanici.uid)
            // Uygulama açıkken oturum varsa token'ı da güncel tutalım
            fcmTokenGuncelle()
        }
    }

    fun kayitOl(email: String, sifre: String, rol: String) {
        val temizEmail = email.trim()
        val temizSifre = sifre.trim()

        if (temizEmail.isEmpty() || temizSifre.isEmpty()) {
            _mesaj.value = "Lütfen tüm alanları doldurun."
            return
        }

        auth.createUserWithEmailAndPassword(temizEmail, temizSifre)
            .addOnSuccessListener { sonuc ->
                val uid = sonuc.user?.uid
                if (uid != null) {
                    val kullaniciVerisi = hashMapOf(
                        "email" to temizEmail,
                        "rol" to rol,
                        "kayitTarihi" to System.currentTimeMillis()
                    )

                    firestore.collection("kullanicilar").document(uid).set(kullaniciVerisi)
                        .addOnSuccessListener {
                            _mesaj.value = "Kayıt başarılı! Yönlendiriliyorsunuz..."
                            _kullaniciRolu.value = rol
                            // Yeni kayıt olan kullanıcının da token'ını anında kaydet
                            fcmTokenGuncelle()
                        }
                        .addOnFailureListener {
                            _mesaj.value = "Veritabanı kaydı başarısız oldu."
                        }
                }
            }
            .addOnFailureListener { hata ->
                _mesaj.value = hata.message ?: "Kayıt başarısız."
            }
    }

    fun girisYap(email: String, sifre: String) {
        val temizEmail = email.trim()
        val temizSifre = sifre.trim()

        if (temizEmail.isEmpty() || temizSifre.isEmpty()) {
            _mesaj.value = "Lütfen tüm alanları doldurun."
            return
        }

        auth.signInWithEmailAndPassword(temizEmail, temizSifre)
            .addOnSuccessListener { sonuc ->
                val uid = sonuc.user?.uid
                if (uid != null) {
                    roluGetir(uid)
                    // Giriş yapan kullanıcının token'ını güncelleyelim ki bildirimler sorunsuz gitsin
                    fcmTokenGuncelle()
                }
            }
            .addOnFailureListener { hata ->
                _mesaj.value = hata.message ?: "Giriş başarısız."
            }
    }

    private fun roluGetir(uid: String) {
        firestore.collection("kullanicilar").document(uid).get()
            .addOnSuccessListener { belge ->
                if (belge.exists()) {
                    val rol = belge.getString("rol") ?: "musteri"
                    _mesaj.value = "Giriş başarılı!"
                    _kullaniciRolu.value = rol
                } else {
                    _mesaj.value = "Kullanıcı profili bulunamadı."
                    cikisYap()
                }
            }
            .addOnFailureListener {
                _mesaj.value = "Kullanıcı bilgileri alınamadı."
            }
    }

    fun fcmTokenGuncelle() {
        val userId = auth.currentUser?.uid ?: return

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            val token = task.result

            firestore.collection("kullanicilar").document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener {
                    println("FCM Token güncellendi: $token")
                }
                .addOnFailureListener {
                    // Eğer belgede henüz "fcmToken" alanı yoksa update hata verebilir,
                    // o yüzden set(..., SetOptions.merge()) ile de destekleyebiliriz:
                    firestore.collection("kullanicilar").document(userId)
                        .set(hashMapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                }
        }
    }

    fun cikisYap() {
        auth.signOut()
        _kullaniciRolu.value = null
    }
}