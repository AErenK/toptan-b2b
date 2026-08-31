package com.example.toptan.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // Ekrandaki hata veya başarı mesajlarını tutmak için
    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj

    // Kullanıcının giriş yapıp yapmadığını tutar
    private val _girisBasarili = MutableStateFlow(false)
    val girisBasarili: StateFlow<Boolean> = _girisBasarili

    init {
        // Uygulama açıldığında kullanıcı zaten giriş yapmış mı kontrol et
        if (auth.currentUser != null) {
            _girisBasarili.value = true
        }
    }

    fun kayitOl(email: String, sifre: String) {
        // Baştaki ve sondaki görünmez boşlukları temizle
        val temizEmail = email.trim()
        val temizSifre = sifre.trim()

        if (temizEmail.isEmpty() || temizSifre.isEmpty()) {
            _mesaj.value = "Lütfen tüm alanları doldurun."
            return
        }

        auth.createUserWithEmailAndPassword(temizEmail, temizSifre)
            .addOnSuccessListener {
                _mesaj.value = "Kayıt başarılı! Giriş yapılıyor..."
                _girisBasarili.value = true
            }
            .addOnFailureListener { hata ->
                _mesaj.value = hata.message ?: "Kayıt başarısız."
            }
    }

    fun girisYap(email: String, sifre: String) {
        // Baştaki ve sondaki görünmez boşlukları temizle
        val temizEmail = email.trim()
        val temizSifre = sifre.trim()

        if (temizEmail.isEmpty() || temizSifre.isEmpty()) {
            _mesaj.value = "Lütfen tüm alanları doldurun."
            return
        }

        auth.signInWithEmailAndPassword(temizEmail, temizSifre)
            .addOnSuccessListener {
                _mesaj.value = "Giriş başarılı!"
                _girisBasarili.value = true
            }
            .addOnFailureListener { hata ->
                _mesaj.value = hata.message ?: "Giriş başarısız."
            }
    }

    fun cikisYap() {
        auth.signOut()
        _girisBasarili.value = false
    }
}