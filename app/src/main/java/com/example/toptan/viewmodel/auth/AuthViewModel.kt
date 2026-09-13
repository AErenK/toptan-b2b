package com.example.toptan.viewmodel.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj

    private val _kullaniciRolu = MutableStateFlow<String?>(null)
    val kullaniciRolu: StateFlow<String?> = _kullaniciRolu

    // YENİ: Alt Personel Yetkilendirmesi İçin State
    private val _altRol = MutableStateFlow<String?>("patron")
    val altRol: StateFlow<String?> = _altRol

    init {
        auth.currentUser?.let { kullanici ->
            roluGetir(kullanici.uid)
            fcmTokenGuncelle()
        }
    }

    fun kayitOl(email: String, sifre: String, rol: String, sirketUnvani: String, vergiDairesi: String, vergiNo: String, adres: String, yetkiliKisi: String, telefon: String) {
        val temizEmail = email.trim()
        val temizSifre = sifre.trim()
        if (temizEmail.isEmpty() || temizSifre.isEmpty()) return

        auth.createUserWithEmailAndPassword(temizEmail, temizSifre).addOnSuccessListener { sonuc ->
            val uid = sonuc.user?.uid
            if (uid != null) {
                val kullaniciVerisi = hashMapOf(
                    "email" to temizEmail,
                    "rol" to rol,
                    "altRol" to "patron", // Sisteme ilk kayıt olan daima Patrondur
                    "sirketUnvani" to sirketUnvani.trim(),
                    "vergiDairesi" to vergiDairesi.trim(),
                    "vergiNo" to vergiNo.trim(),
                    "adres" to adres.trim(),
                    "yetkiliKisi" to yetkiliKisi.trim(),
                    "telefon" to telefon.trim(),
                    "kayitTarihi" to System.currentTimeMillis()
                )
                firestore.collection("kullanicilar").document(uid).set(kullaniciVerisi).addOnSuccessListener {
                    _kullaniciRolu.value = rol
                    _altRol.value = "patron"
                    fcmTokenGuncelle()
                }
            }
        }.addOnFailureListener { _mesaj.value = it.message }
    }

    fun girisYap(email: String, sifre: String) {
        val temizEmail = email.trim()
        val temizSifre = sifre.trim()
        if (temizEmail.isEmpty() || temizSifre.isEmpty()) return

        auth.signInWithEmailAndPassword(temizEmail, temizSifre).addOnSuccessListener { sonuc ->
            sonuc.user?.uid?.let {
                roluGetir(it)
                fcmTokenGuncelle()
            }
        }.addOnFailureListener { _mesaj.value = it.message }
    }

    private fun roluGetir(uid: String) {
        firestore.collection("kullanicilar").document(uid).get().addOnSuccessListener { belge ->
            if (belge.exists()) {
                _kullaniciRolu.value = belge.getString("rol") ?: "musteri"
                // YENİ: Firebase'den alt rolü çekiyoruz (Yoksa patron kabul edilir)
                _altRol.value = belge.getString("altRol") ?: "patron"
            } else {
                cikisYap()
            }
        }
    }

    fun fcmTokenGuncelle() {
        val userId = auth.currentUser?.uid ?: return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firestore.collection("kullanicilar").document(userId)
                    .set(hashMapOf("fcmToken" to task.result), SetOptions.merge())
            }
        }
    }

    fun sifreSifirla(email: String) {
        if (email.isBlank()) return
        auth.sendPasswordResetEmail(email.trim()).addOnSuccessListener {
            _mesaj.value = "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi! (başarılı)"
        }
    }

    fun cikisYap() {
        auth.signOut()
        _kullaniciRolu.value = null
        _altRol.value = null
    }
}