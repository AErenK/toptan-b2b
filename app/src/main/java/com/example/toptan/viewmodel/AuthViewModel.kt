package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance() // Firestore eklendi

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj

    // Sadece giriş başarısını değil, kullanıcının rolünü de tutuyoruz ("musteri" veya "toptanci")
    private val _kullaniciRolu = MutableStateFlow<String?>(null)
    val kullaniciRolu: StateFlow<String?> = _kullaniciRolu

    init {
        // Uygulama açıldığında oturum varsa rolünü veritabanından çek
        auth.currentUser?.let { kullanici ->
            roluGetir(kullanici.uid)
        }
    }

    // Kayıt ol fonksiyonuna 'rol' parametresi eklendi
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
                    // Kullanıcı başarıyla oluştu, şimdi Firestore'a rolünü kaydedelim
                    val kullaniciVerisi = hashMapOf(
                        "email" to temizEmail,
                        "rol" to rol, // "musteri" veya "toptanci"
                        "kayitTarihi" to System.currentTimeMillis()
                    )

                    firestore.collection("kullanicilar").document(uid).set(kullaniciVerisi)
                        .addOnSuccessListener {
                            _mesaj.value = "Kayıt başarılı! Yönlendiriliyorsunuz..."
                            _kullaniciRolu.value = rol
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
                    // Giriş başarılı, Firestore'dan kullanıcının rolünü öğren
                    roluGetir(uid)
                }
            }
            .addOnFailureListener { hata ->
                _mesaj.value = hata.message ?: "Giriş başarısız."
            }
    }

    // Firestore'dan rol bilgisini çeken yardımcı fonksiyon
    private fun roluGetir(uid: String) {
        firestore.collection("kullanicilar").document(uid).get()
            .addOnSuccessListener { belge ->
                if (belge.exists()) {
                    val rol = belge.getString("rol") ?: "musteri" // Rol yoksa varsayılan müşteri olsun
                    _mesaj.value = "Giriş başarılı!"
                    _kullaniciRolu.value = rol
                } else {
                    _mesaj.value = "Kullanıcı profili bulunamadı."
                    cikisYap() // Güvenlik için profili olmayan hesaptan çıkış yap
                }
            }
            .addOnFailureListener {
                _mesaj.value = "Kullanıcı bilgileri alınamadı."
            }
    }

    fun cikisYap() {
        auth.signOut()
        _kullaniciRolu.value = null
    }
}