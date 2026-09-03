package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLogoutClick: () -> Unit
) {
    // ViewModel'den rolü dinliyoruz
    val rol by authViewModel.kullaniciRolu.collectAsState()

    // Firebase'den aktif kullanıcının e-postasını anlık olarak alıyoruz
    val aktifKullanici = FirebaseAuth.getInstance().currentUser
    val userEmail = aktifKullanici?.email ?: "Bilinmeyen Kullanıcı"

    // Ekranda güzel görünmesi için rol ismini düzeltiyoruz
    val rolMetni = if (rol == "toptanci") "Toptancı" else "Dükkan Sahibi"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Diğer ekranlarla uyumlu arka plan
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Profilim", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1565C0))

        Spacer(modifier = Modifier.height(32.dp))

        // --- PROFİL AVATARI ---
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFF1565C0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profil Resmi", tint = Color.White, modifier = Modifier.size(60.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- HESAP BİLGİLERİ KARTI ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Hesap Bilgileri", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // E-Posta Satırı
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = userEmail, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rol Satırı
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rol", tint = Color(0xFFF57C00)) // Turuncu yıldız
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Hesap Türü: ", fontSize = 16.sp, color = Color.Gray)
                    Text(text = rolMetni, fontSize = 16.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // Çıkış butonunu en aşağı iter

        // --- ÇIKIŞ YAP BUTONU ---
        Button(
            onClick = {
                authViewModel.cikisYap()
                onLogoutClick()
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Kırmızı renk
        ) {
            Text("Oturumu Kapat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}