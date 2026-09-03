package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var secilenRol by remember { mutableStateOf("musteri") }

    val mesaj by viewModel.mesaj.collectAsState()
    val kullaniciRolu by viewModel.kullaniciRolu.collectAsState()

    LaunchedEffect(kullaniciRolu) {
        kullaniciRolu?.let { rol ->
            onRegisterSuccess(rol)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hesap Oluştur",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Toptan ağımıza hemen katılın", color = Color(0xFF64748B), fontSize = 15.sp)

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta Adresi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = sifre,
            onValueChange = { sifre = it },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2563EB),
                unfocusedBorderColor = Color(0xFFE2E8F0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- MODERN ROL SEÇİM ALANI ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Hesap Türünüzü Seçin", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = secilenRol == "musteri",
                            onClick = { secilenRol = "musteri" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                        )
                        Text("Dükkan Sahibi", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = secilenRol == "toptanci",
                            onClick = { secilenRol = "toptanci" },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                        )
                        Text("Toptancı", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.kayitOl(email, sifre, secilenRol) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Kayıt Ol", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Zaten hesabın var mı? Giriş Yap", fontSize = 15.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        }

        mesaj?.let { bilgiMesaji ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = bilgiMesaji,
                color = if (bilgiMesaji.contains("başarılı")) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}