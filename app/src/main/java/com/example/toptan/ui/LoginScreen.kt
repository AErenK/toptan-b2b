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
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: (String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }

    val mesaj by viewModel.mesaj.collectAsState()
    val kullaniciRolu by viewModel.kullaniciRolu.collectAsState()

    LaunchedEffect(kullaniciRolu) {
        kullaniciRolu?.let { rol ->
            onLoginSuccess(rol)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Çok hafif modern arka plan
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Modern Logo / Başlık Alanı
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFFE0E7FF), shape = RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("B2B", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Hoş Geldiniz",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Dükkanınız için toptan alışveriş", color = Color(0xFF64748B), fontSize = 15.sp)

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

        mesaj?.let {
            Text(
                text = it,
                color = if (it.contains("başarılı")) Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = { viewModel.girisYap(email, sifre) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Daha dolgun modern buton
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Giriş Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Hesabın yok mu? Kayıt Ol", fontSize = 15.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
        }
    }
}