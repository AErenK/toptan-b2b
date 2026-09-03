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
    val rol by authViewModel.kullaniciRolu.collectAsState()
    val aktifKullanici = FirebaseAuth.getInstance().currentUser
    val userEmail = aktifKullanici?.email ?: "Bilinmeyen Kullanıcı"
    val rolMetni = if (rol == "toptanci") "Toptancı" else "Dükkan Sahibi"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "Profilim", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFFDBEAFE), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profil Resmi", tint = Color(0xFF2563EB), modifier = Modifier.size(50.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Hesap Bilgileri", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFF64748B))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = userEmail, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rol", tint = Color(0xFFD97706))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Hesap Türü: ", fontSize = 15.sp, color = Color(0xFF64748B))
                    Text(text = rolMetni, fontSize = 15.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                authViewModel.cikisYap()
                onLogoutClick()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
        ) {
            Text("Oturumu Kapat", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}