package com.example.toptan.ui.musteri

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onNavigateToSiparislerim: () -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userEmail = user?.email ?: "Kullanıcı bulunamadı"

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var isCompanyInfoExpanded by remember { mutableStateOf(false) }

    val maxLimit = 50000.0
    var cariLimit by remember { mutableStateOf(maxLimit) }

    var sirketUnvani by remember { mutableStateOf("Yükleniyor...") }
    var vergiDairesi by remember { mutableStateOf("-") }
    var vergiNo by remember { mutableStateOf("-") }
    var adres by remember { mutableStateOf("-") }

    var yetkiliKisi by remember { mutableStateOf("-") }
    var telefon by remember { mutableStateOf("-") }

    val kullanilanLimit = maxLimit - cariLimit
    val kullanimOrani = (kullanilanLimit / maxLimit).toFloat()

    val formatliLimit = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(cariLimit)
    val formatliKullanilan = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(kullanilanLimit)

    val (vipSeviye, vipRenk) = when {
        kullanilanLimit >= 30000.0 -> Pair("PLATINUM", Color(0xFFE5E7EB))
        kullanilanLimit >= 15000.0 -> Pair("GOLD", Color(0xFFF59E0B))
        else -> Pair("SILVER", Color(0xFF94A3B8))
    }

    // Hem Cari Limiti Hem de Yeni Eklediğimiz Şirket Bilgilerini Dinliyoruz
    LaunchedEffect(user?.uid) {
        if (user != null) {
            FirebaseFirestore.getInstance().collection("kullanicilar").document(user.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        cariLimit = snapshot.getDouble("cariLimit") ?: maxLimit

                        // Kayıt ekranından gelen verileri okuyoruz
                        sirketUnvani = snapshot.getString("sirketUnvani") ?: "Şirket Adı Belirtilmemiş"
                        vergiDairesi = snapshot.getString("vergiDairesi") ?: "-"
                        vergiNo = snapshot.getString("vergiNo") ?: "-"
                        adres = snapshot.getString("adres") ?: "-"
                        // --- YENİ EKLENEN İLETİŞİM BİLGİLERİ ---
                        yetkiliKisi = snapshot.getString("yetkiliKisi") ?: "-"
                        telefon = snapshot.getString("telefon") ?: "-"
                    }
                }
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
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Color(0xFF0F172A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(54.dp).clip(CircleShape).background(Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profil", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Müşteri Hesabı", color = Color(0xFF94A3B8), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userEmail.substringBefore("@"),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                IconButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış", tint = Color(0xFFEF4444))
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 130.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // DİNAMİK CARİ KART
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(200.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF000000))))
                        .padding(24.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = vipRenk, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("$vipSeviye ÜYE", color = vipRenk, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                            }
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(28.dp))
                        }

                        Column {
                            Text("Kullanılabilir Cari Limit", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$formatliLimit ₺", color = Color(0xFF10B981), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Kullanım: $formatliKullanilan ₺", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                Text("%${(kullanimOrani * 100).toInt()}", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { kullanimOrani },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(10.dp)),
                                color = vipRenk,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DİNAMİK ŞİRKET BİLGİLERİ KARTI (Veriler Firebase'den geliyor)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clickable { isCompanyInfoExpanded = !isCompanyInfoExpanded },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(Color(0xFFF3E8FF), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text("Fatura ve Şirket Bilgileri", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                        }
                        Icon(
                            imageVector = if (isCompanyInfoExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null, tint = Color(0xFF94A3B8)
                        )
                    }

                    AnimatedVisibility(
                        visible = isCompanyInfoExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(16.dp))
                            CompanyInfoRow("Şirket Ünvanı:", sirketUnvani)
                            CompanyInfoRow("Vergi Dairesi:", vergiDairesi)
                            CompanyInfoRow("Vergi No:", vergiNo)
                            CompanyInfoRow("Fatura Adresi:", adres)
                            // --- YENİ EKLENEN İLETİŞİM BİLGİLERİ ---
                            CompanyInfoRow("Yetkili:", yetkiliKisi)
                            CompanyInfoRow("İletişim:", telefon)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İŞLEVSEL HIZLI MENÜLER
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                ActionMenuCard(
                    icon = Icons.Default.ShoppingBag,
                    title = "Geçmiş Siparişlerim",
                    subtitle = "Tüm alımlarınızı ve faturalarınızı görün",
                    iconColor = Color(0xFF2563EB),
                    onClick = onNavigateToSiparislerim
                )
                Spacer(modifier = Modifier.height(12.dp))
                ActionMenuCard(
                    icon = Icons.Default.HeadsetMic,
                    title = "Müşteri Hizmetleri",
                    subtitle = "Toptancınızla veya destek ekibiyle görüşün",
                    iconColor = Color(0xFFD97706),
                    onClick = { showSupportDialog = true }
                )
                Spacer(modifier = Modifier.height(12.dp))
                ActionMenuCard(
                    icon = Icons.Default.Info,
                    title = "Uygulama Hakkında",
                    subtitle = "Sürüm bilgileri ve sözleşmeler",
                    iconColor = Color(0xFF10B981),
                    onClick = { showInfoDialog = true }
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // --- DIALOGLAR ---

    // 1. ÇIKIŞ YAP DİALOGU
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Çıkış Yap", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
            text = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?", fontSize = 14.sp, color = Color(0xFF64748B)) },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        FirebaseAuth.getInstance().signOut()
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Evet, Çıkış Yap", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("İptal", color = Color(0xFF64748B), fontWeight = FontWeight.Bold) }
            }
        )
    }

    // 2. MÜŞTERİ HİZMETLERİ DİALOGU
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text("Müşteri Hizmetleri", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
            text = {
                Column {
                    Text("Destek ekibimize çalışma saatleri içinde ulaşabilirsiniz.", fontSize = 14.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("📞 Telefon: 0850 123 45 67", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✉️ E-Posta: destek@toptanb2b.com", fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = { showSupportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Tamam", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        )
    }

    // 3. UYGULAMA HAKKINDA DİALOGU
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Uygulama Hakkında", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
            text = {
                Column {
                    Text("Toptan B2B Sipariş ve Cari Yönetim Sistemi", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sürüm: 1.0.0 (Premium Edit)", fontSize = 14.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tüm hakları saklıdır © 2026", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = { showInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Kapat", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
fun CompanyInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF64748B), fontSize = 13.sp)
        Text(text = value, color = Color(0xFF1E293B), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ActionMenuCard(icon: ImageVector, title: String, subtitle: String, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(46.dp).background(iconColor.copy(alpha = 0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "Git", tint = Color(0xFFCBD5E1), modifier = Modifier.size(22.dp))
        }
    }
}