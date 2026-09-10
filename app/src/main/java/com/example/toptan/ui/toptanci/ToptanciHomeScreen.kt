package com.example.toptan.ui.toptanci

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.model.Urun
import com.example.toptan.viewmodel.auth.AuthViewModel
import com.example.toptan.viewmodel.toptanci.ToptanciViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ToptanciHomeScreen(
    viewModel: ToptanciViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onLogoutClick: () -> Unit,
    onNavigateToSiparisler: () -> Unit,
    onNavigateToUrunEkle: () -> Unit,
    onNavigateToKatalog: () -> Unit,
    onNavigateToMusteriler: () -> Unit
) {
    val toplamUrun by viewModel.toplamUrunSayisi.collectAsState()
    val bekleyenSiparis by viewModel.bekleyenSiparisSayisi.collectAsState()
    val toplamCiro by viewModel.toplamCiro.collectAsState()
    val tumUrunler by viewModel.toptanciUrunleri.collectAsState()

    // YENİ: Analizler ve Ayarlar
    val minLimit by viewModel.minSiparisTutari.collectAsState()
    val enIyiMusteriler by viewModel.enIyiMusteriler.collectAsState()
    val enCokSatanlar by viewModel.enCokSatanUrunler.collectAsState()
    var ayarlarDialogAcik by remember { mutableStateOf(false) }
    var yeniLimitGirdisi by remember { mutableStateOf("") }

    val formatliCiro = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(toplamCiro)
    val mevcutKullanici = FirebaseAuth.getInstance().currentUser
    val toptanciEmail = mevcutKullanici?.email ?: "Toptancı"

    val kritikStokluUrunler = remember(tumUrunler) { tumUrunler.filter { it.stok <= 20 } }

    LaunchedEffect(Unit) {
        viewModel.istatistikleriGetir()
        viewModel.toptanciUrunleriniGetir()
        viewModel.toptanciAyarlariniGetir()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        // --- ÜST LACİVERT ALAN ---
        Box(
            modifier = Modifier.fillMaxWidth().height(260.dp)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(Color(0xFF0F172A))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hoş Geldiniz,", color = Color(0xFF94A3B8), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = toptanciEmail.substringBefore("@"), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                }
                Row {
                    // YENİ: AYARLAR BUTONU
                    IconButton(
                        onClick = { yeniLimitGirdisi = minLimit.toInt().toString(); ayarlarDialogAcik = true },
                        modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) { Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = Color.White) }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { authViewModel.cikisYap(); onLogoutClick() },
                        modifier = Modifier.size(42.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Çıkış", tint = Color(0xFFEF4444)) }
                }
            }
        }

        // --- İÇERİK ALANI ---
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 120.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // İSTATİSTİK KARTI
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Genel Durum", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$formatliCiro ₺", color = Color(0xFF16A34A), fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Toplam Teslim Edilen Ciro", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(icon = Icons.Default.ListAlt, title = "Bekleyen", value = "$bekleyenSiparis", color = Color(0xFFD97706))
                        StatItem(icon = Icons.Default.Inventory, title = "Katalog", value = "$toplamUrun", color = Color(0xFF2563EB))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // HIZLI İŞLEMLER (GRID)
            Text("Yönetim Paneli", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    GridActionCard(modifier = Modifier.weight(1f), title = "Siparişler", icon = Icons.Default.ListAlt, bgColor = Color(0xFFDBEAFE), iconColor = Color(0xFF2563EB), onClick = onNavigateToSiparisler)
                    GridActionCard(modifier = Modifier.weight(1f), title = "Müşterilerim", icon = Icons.Default.Group, bgColor = Color(0xFFDCFCE7), iconColor = Color(0xFF16A34A), onClick = onNavigateToMusteriler)
                    GridActionCard(modifier = Modifier.weight(1f), title = "Katalog", icon = Icons.Default.Inventory, bgColor = Color(0xFFF3E8FF), iconColor = Color(0xFF9333EA), onClick = onNavigateToKatalog)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(70.dp).clickable { onNavigateToUrunEkle() },
                    shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kataloğa Yeni Ürün Ekle", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // YENİ: SATIŞ ANALİZLERİ
            if (enIyiMusteriler.isNotEmpty() || enCokSatanlar.isNotEmpty()) {
                Text("📊 Satış Analizleri", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B), modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // En Çok Satan Ürünler Tablosu
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔥 Top 3 Ürün", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD97706))
                            Spacer(modifier = Modifier.height(12.dp))
                            enCokSatanlar.forEach { (urunAdi, adet) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(urunAdi, fontSize = 12.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text("$adet Ad.", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }

                    // En İyi Müşteriler Tablosu
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("⭐ Top 3 Müşteri", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2563EB))
                            Spacer(modifier = Modifier.height(12.dp))
                            enIyiMusteriler.forEach { (musteri, ciro) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(musteri, fontSize = 12.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text("${NumberFormat.getNumberInstance(Locale("tr","TR")).format(ciro)} ₺", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // AYARLAR (MİNİMUM SEPET LİMİTİ) DİALOGU
    if (ayarlarDialogAcik) {
        AlertDialog(
            onDismissRequest = { ayarlarDialogAcik = false },
            title = { Text("Toptancı Ayarları", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Müşterilerin sizden sipariş verebilmesi için sepette ulaşmaları gereken minimum tutarı belirleyin.", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = yeniLimitGirdisi, onValueChange = { yeniLimitGirdisi = it },
                        label = { Text("Minimum Sepet Tutarı (₺)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val tutar = yeniLimitGirdisi.toDoubleOrNull() ?: 0.0
                    viewModel.minSiparisTutariniGuncelle(tutar)
                    ayarlarDialogAcik = false
                }) { Text("Kaydet") }
            },
            dismissButton = { TextButton(onClick = { ayarlarDialogAcik = false }) { Text("İptal") } }
        )
    }
}

// ... GridActionCard ve StatItem fonksiyonları aynı kalacak ...
@Composable fun StatItem(icon: ImageVector, title: String, value: String, color: Color) { Row(verticalAlignment = Alignment.CenterVertically) { Box(modifier = Modifier.size(42.dp).background(color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(20.dp)) }; Spacer(modifier = Modifier.width(12.dp)); Column { Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E293B)); Text(title, color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.Medium) } } }
@Composable fun GridActionCard(modifier: Modifier = Modifier, title: String, icon: ImageVector, bgColor: Color, iconColor: Color, onClick: () -> Unit) { Card(modifier = modifier.height(110.dp).clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) { Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Box(modifier = Modifier.size(48.dp).background(bgColor, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(24.dp)) }; Spacer(modifier = Modifier.height(10.dp)); Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis) } } }