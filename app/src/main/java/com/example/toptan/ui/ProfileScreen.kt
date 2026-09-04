package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit
) {
    // Firebase'den anlık giriş yapmış kullanıcının bilgilerini alıyoruz
    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email ?: "Kullanıcı bulunamadı"

    // Çıkış yapma uyarı penceresini (Dialog) gösterip gizlemek için durum (state)
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilim", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 1. PROFIL KARTI ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mavi Yuvarlak İçinde Kullanıcı İkonu (Avatar)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)), // Açık Mavi Arkaplan
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil Resmi",
                            tint = Color(0xFF1976D2), // Koyu Mavi İkon
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Firebase'den Gelen E-posta
                    Text(text = userEmail, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Aktif Kullanıcı", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. HIZLI ERİŞİM MENÜSÜ ---
            ProfileMenuItem(icon = Icons.Default.Settings, title = "Hesap Ayarları") { /* Şimdilik boş */ }
            Spacer(modifier = Modifier.height(12.dp))
            ProfileMenuItem(icon = Icons.Default.SupportAgent, title = "Yardım ve Destek") { /* Şimdilik boş */ }

            Spacer(modifier = Modifier.weight(1f)) // Çıkış butonunu ekranın en altına iter

            // --- 3. ÇIKIŞ YAP BUTONU ---
            Button(
                onClick = { showLogoutDialog = true }, // Butona basınca Dialog'u aç
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)), // Açık kırmızı
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış", tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Çıkış Yap", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }

    // --- 4. ÇIKIŞ ONAY PENCERESİ (DIALOG) ---
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false }, // Dışarı tıklanırsa kapat
            title = { Text(text = "Çıkış Yap", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        FirebaseAuth.getInstance().signOut() // Firebase'den çıkış yap
                        onLogoutClick() // MainScreen'deki navigasyonu tetikle
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Kırmızı
                ) {
                    Text("Evet, Çıkış Yap", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("İptal", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// Menü Elemanları İçin Yardımcı Tasarım Fonksiyonu
@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF1565C0))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
        }
    }
}