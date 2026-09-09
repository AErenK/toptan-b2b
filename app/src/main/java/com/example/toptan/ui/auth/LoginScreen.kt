package com.example.toptan.ui.auth

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

    // Şifremi Unuttum Penceresi Kontrolü
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }

    val mesaj by viewModel.mesaj.collectAsState()
    val kullaniciRolu by viewModel.kullaniciRolu.collectAsState()

    val focusManager = LocalFocusManager.current

    // Anlık (Real-time) Doğrulama Kontrolleri
    val isEmailValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isFormValid = email.isNotEmpty() && sifre.isNotEmpty() && isEmailValid

    LaunchedEffect(kullaniciRolu) {
        kullaniciRolu?.let { rol ->
            onLoginSuccess(rol)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Üst Arka Plan Gradyanı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF2563EB).copy(alpha = 0.08f), Color.Transparent)))
        )

        // İçerik (Klavye açıldığında kaydırılabilir yapı - Ergonomi)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()), // Klavye dostu
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "Tekrar Hoş Geldiniz",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Toptan B2B ağınıza giriş yaparak ticarete devam edin",
                color = Color(0xFF64748B),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // E-posta Alanı (Akıllı Doğrulamalı)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-posta Adresi", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B)) },
                isError = !isEmailValid, // Hata durumu
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    errorBorderColor = Color(0xFFEF4444)
                ),
                singleLine = true
            )

            // Canlı Hata Mesajı
            AnimatedVisibility(visible = !isEmailValid) {
                Text("Lütfen geçerli bir e-posta adresi girin", color = Color(0xFFEF4444), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp).fillMaxWidth(), textAlign = TextAlign.Start)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Şifre Alanı
            OutlinedTextField(
                value = sifre,
                onValueChange = { sifre = it },
                label = { Text("Şifre", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (isFormValid) viewModel.girisYap(email, sifre)
                }),
                modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            // Şifremi Unuttum Butonu
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { showForgotPasswordDialog = true }) {
                    Text("Şifremi Unuttum", color = Color(0xFF2563EB), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Firebase'den gelen Mesaj (Başarı veya Hata)
            AnimatedVisibility(visible = !mesaj.isNullOrEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (mesaj.orEmpty().contains("başarılı")) Color(0xFFDCFCE7) else Color(0xFFFEF2F2)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        if (!mesaj.orEmpty().contains("başarılı")) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = mesaj?.replace(" (başarılı)", "") ?: "",
                            color = if (mesaj.orEmpty().contains("başarılı")) Color(0xFF16A34A) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (!mesaj.isNullOrEmpty()) Spacer(modifier = Modifier.height(16.dp))

            // Giriş Yap Butonu (Form hatalıysa buton pasif olur - Gelişmiş UX)
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.girisYap(email, sifre)
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    disabledContainerColor = Color(0xFF94A3B8) // Pasif rengi
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Sisteme Giriş Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Kayıt Ol Yönlendirmesi
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("İşletmeniz kayıtlı değil mi? ", color = Color(0xFF64748B), fontSize = 14.sp)
                Text(
                    text = "Hemen Kurumsal Kayıt Olun",
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- ŞİFRE SIFIRLAMA (FORGOT PASSWORD) DİALOGU ---
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Şifrenizi mi Unuttunuz?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
            text = {
                Column {
                    Text("Kayıtlı e-posta adresinizi girin. Size güvenli bir şifre sıfırlama bağlantısı göndereceğiz.", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("E-posta Adresi") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sifreSifirla(resetEmail)
                        showForgotPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Bağlantı Gönder", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("İptal", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}