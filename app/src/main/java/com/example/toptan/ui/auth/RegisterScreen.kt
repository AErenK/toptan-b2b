package com.example.toptan.ui.auth

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit
) {
    // Giriş Bilgileri
    var email by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var secilenRol by remember { mutableStateOf("musteri") }

    // Şirket Bilgileri
    var sirketUnvani by remember { mutableStateOf("") }
    var vergiDairesi by remember { mutableStateOf("") }
    var vergiNo by remember { mutableStateOf("") }
    var adres by remember { mutableStateOf("") }

    // YENİ: İletişim Bilgileri
    var yetkiliKisi by remember { mutableStateOf("") }
    var telefon by remember { mutableStateOf("") }

    val mesaj by viewModel.mesaj.collectAsState()
    val kullaniciRolu by viewModel.kullaniciRolu.collectAsState()

    val focusManager = LocalFocusManager.current

    // --- GELİŞMİŞ ŞİFRE VE FORM DOĞRULAMA (VALIDATION) ---
    val isEmailValid = email.isEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // Şifre Kuralları
    val hasMinLength = sifre.length >= 8
    val hasUpper = sifre.any { it.isUpperCase() }
    val hasNumber = sifre.any { it.isDigit() }
    val hasSpecial = sifre.any { !it.isLetterOrDigit() }
    val isPasswordValid = hasMinLength && hasUpper && hasNumber && hasSpecial

    val isFormValid = email.isNotEmpty() && isEmailValid &&
            sifre.isNotEmpty() && isPasswordValid &&
            sirketUnvani.isNotBlank() && vergiDairesi.isNotBlank() &&
            vergiNo.isNotBlank() && adres.isNotBlank() &&
            yetkiliKisi.isNotBlank() && telefon.isNotBlank()

    LaunchedEffect(kullaniciRolu) {
        kullaniciRolu?.let { rol ->
            onRegisterSuccess(rol)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF2563EB).copy(alpha = 0.06f), Color.Transparent)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text("Kurumsal Kayıt 🚀", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(6.dp))
            Text("İşletmenizi ve iletişim bilgilerinizi sisteme ekleyin", color = Color(0xFF64748B), fontSize = 14.sp, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(28.dp))

            // --- 1. TİCARİ BİLGİLER ALANI ---
            Text("İşletme Bilgileri", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = sirketUnvani, onValueChange = { sirketUnvani = it }, label = { Text("Firma / Dükkan Adı", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF64748B)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = vergiDairesi, onValueChange = { vergiDairesi = it }, label = { Text("Vergi Dairesi", color = Color(0xFF64748B)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                    modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(16.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
                OutlinedTextField(
                    value = vergiNo, onValueChange = { vergiNo = it }, label = { Text("Vergi No", color = Color(0xFF64748B)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = Color(0xFF64748B)) },
                    modifier = Modifier.weight(1f).height(58.dp), shape = RoundedCornerShape(16.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = adres, onValueChange = { adres = it }, label = { Text("Tam Fatura / Teslimat Adresi", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- YENİ: 2. İLETİŞİM BİLGİLERİ ALANI ---
            Text("İletişim Bilgileri", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = yetkiliKisi, onValueChange = { yetkiliKisi = it }, label = { Text("Yetkili Ad Soyad", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = telefon, onValueChange = { telefon = it }, label = { Text("Cep Telefonu", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(24.dp))

            // --- 3. GİRİŞ BİLGİLERİ ALANI ---
            Text("Sistem Giriş Bilgileri", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email, onValueChange = { email = it }, label = { Text("E-posta Adresi", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF64748B)) },
                isError = !isEmailValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, errorBorderColor = Color(0xFFEF4444))
            )
            AnimatedVisibility(visible = !isEmailValid) {
                Text("Geçerli bir e-posta adresi girin", color = Color(0xFFEF4444), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp).fillMaxWidth(), textAlign = TextAlign.Start)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = sifre, onValueChange = { sifre = it }, label = { Text("Şifre", color = Color(0xFF64748B)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64748B)) },
                visualTransformation = PasswordVisualTransformation(),
                isError = sifre.isNotEmpty() && !isPasswordValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (isFormValid) viewModel.kayitOl(email, sifre, secilenRol, sirketUnvani, vergiDairesi, vergiNo, adres, yetkiliKisi, telefon)
                }),
                modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(16.dp), singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0), focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, errorBorderColor = Color(0xFFEF4444))
            )

            // YENİ: Canlı Şifre Güvenlik Kontrol Paneli
            AnimatedVisibility(visible = sifre.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Şifre Güvenlik Kriterleri:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(8.dp))
                        PasswordRequirementItem(text = "En az 8 karakter", isValid = hasMinLength)
                        PasswordRequirementItem(text = "En az 1 büyük harf (A-Z)", isValid = hasUpper)
                        PasswordRequirementItem(text = "En az 1 rakam (0-9)", isValid = hasNumber)
                        PasswordRequirementItem(text = "En az 1 özel karakter (*, ., @ vb.)", isValid = hasSpecial)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ROL SEÇİMİ
            Card(
                shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("İşletme Türünüz", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { secilenRol = "musteri" }) {
                            RadioButton(selected = secilenRol == "musteri", onClick = { secilenRol = "musteri" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB)))
                            Text("Dükkan / Perakende", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { secilenRol = "toptanci" }) {
                            RadioButton(selected = secilenRol == "toptanci", onClick = { secilenRol = "toptanci" }, colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB)))
                            Text("Toptancı / Depo", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = !mesaj.isNullOrEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    color = if (mesaj.orEmpty().contains("başarılı")) Color(0xFFDCFCE7) else Color(0xFFFEF2F2)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        if (!mesaj.orEmpty().contains("başarılı")) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = mesaj ?: "", color = if (mesaj.orEmpty().contains("başarılı")) Color(0xFF16A34A) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (!mesaj.isNullOrEmpty()) Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.kayitOl(email, sifre, secilenRol, sirketUnvani, vergiDairesi, vergiNo, adres, yetkiliKisi, telefon)
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), disabledContainerColor = Color(0xFF94A3B8)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(text = if (isFormValid) "Kurumsal Kaydı Tamamla" else "Bilgileri Eksiksiz Doldurun", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
                Text("Zaten hesabın var mı? Giriş Yap", fontSize = 14.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Şifre kurallarını ekranda yeşil tik veya kırmızı çarpı ile gösteren ufak bileşen
@Composable
fun PasswordRequirementItem(text: String, isValid: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Box(
            modifier = Modifier.size(16.dp).background(if (isValid) Color(0xFF10B981) else Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isValid) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                tint = if (isValid) Color.White else Color(0xFF94A3B8),
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = if (isValid) Color(0xFF10B981) else Color(0xFF64748B))
    }
}