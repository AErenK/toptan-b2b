package com.example.toptan.ui

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
    onRegisterSuccess: (String) -> Unit // Başarılı kayıtta rolü ("musteri" veya "toptanci") ana ekrana iletecek
) {
    var email by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }

    // Varsayılan olarak "Dükkan Sahibi" (musteri) seçili gelsin
    var secilenRol by remember { mutableStateOf("musteri") }

    val mesaj by viewModel.mesaj.collectAsState()
    val kullaniciRolu by viewModel.kullaniciRolu.collectAsState()

    // Kayıt başarılı olduğunda ve rol Firestore'a yazıldığında, kullanıcıyı ilgili panele yönlendir
    LaunchedEffect(kullaniciRolu) {
        kullaniciRolu?.let { rol ->
            onRegisterSuccess(rol)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Yeni Hesap Oluştur",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0) // Senin tasarım dilindeki mavi ton
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta Adresi") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = sifre,
            onValueChange = { sifre = it },
            label = { Text("Şifre") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- ROL SEÇİM ALANI ---
        Text(text = "Hesap Türünüzü Seçin", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dükkan Sahibi Seçeneği
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = secilenRol == "musteri",
                    onClick = { secilenRol = "musteri" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1565C0))
                )
                Text("Dükkan Sahibi")
            }

            // Toptancı Seçeneği
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = secilenRol == "toptanci",
                    onClick = { secilenRol = "toptanci" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1565C0))
                )
                Text("Toptancı")
            }
        }
        // -------------------------

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.kayitOl(email, sifre, secilenRol) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Kayıt Ol", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Zaten hesabın var mı? Giriş Yap", color = Color.Gray)
        }

        // Hata veya Başarı Mesajı Görünümü
        mesaj?.let { bilgiMesaji ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = bilgiMesaji,
                color = if (bilgiMesaji.contains("başarılı")) Color(0xFF2E7D32) else Color.Red,
                fontWeight = FontWeight.Medium
            )
        }
    }
}