package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilim", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kullanıcı Bilgi Kartı
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1565C0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("D", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Demo Kullanıcı", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Dükkan Sahibi", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            }

            item { Text("Hesap Ayarları", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) }

            item { ProfileMenuItem(icon = Icons.Default.Person, text = "Kişisel Bilgiler") }
            item { ProfileMenuItem(icon = Icons.Default.LocationOn, text = "Teslimat Adresleri") }
            item { ProfileMenuItem(icon = Icons.Default.History, text = "Geçmiş Siparişler") }

            item { Text("Diğer", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) }

            item { ProfileMenuItem(icon = Icons.Default.Settings, text = "Uygulama Ayarları") }
            item { ProfileMenuItem(icon = Icons.Default.ExitToApp, text = "Çıkış Yap", tint = Color.Red) }
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, text: String, tint: Color = Color.DarkGray) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = tint)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if (tint == Color.Red) tint else Color.Black)
        }
    }
}