package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.viewmodel.AuthViewModel
import com.example.toptan.viewmodel.ToptanciViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToptanciHomeScreen(
    viewModel: ToptanciViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onLogoutClick: () -> Unit,
    onNavigateToSiparisler: () -> Unit,
    onNavigateToUrunEkle: () -> Unit,
    onNavigateToKatalog: () -> Unit// YENİ: Ürün Ekle sayfasına geçiş rotası
) {
    // Sayfa açıldığında istatistikleri çekmeye başla
    LaunchedEffect(Unit) {
        viewModel.istatistikleriGetir()
    }

    val toplamUrun by viewModel.toplamUrunSayisi.collectAsState()
    val bekleyenSiparis by viewModel.bekleyenSiparisSayisi.collectAsState()
    val toplamCiro by viewModel.toplamCiro.collectAsState()

    val formatliCiro = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(toplamCiro)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toptancı Paneli", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC)),
                actions = {
                    IconButton(onClick = { authViewModel.cikisYap(); onLogoutClick() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış", tint = Color(0xFFEF4444))
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // --- 1. İSTATİSTİK KARTLARI ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Bekleyen",
                    value = "$bekleyenSiparis",
                    icon = Icons.Default.ListAlt,
                    color = Color(0xFFD97706),
                    bgColor = Color(0xFFFEF3C7)
                )
                DashboardCard(
                    modifier = Modifier.weight(1f),
                    title = "Katalog",
                    value = "$toplamUrun",
                    icon = Icons.Default.Inventory,
                    color = Color(0xFF2563EB),
                    bgColor = Color(0xFFDBEAFE)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ciro Kartı (Geniş)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16A34A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Toplam Ciro (Teslim Edilen)", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$formatliCiro ₺", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                    }
                    Icon(Icons.Default.MonetizationOn, contentDescription = "Ciro", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(32.dp))

            // --- 2. HIZLI İŞLEM BUTONLARI ---
            Button(
                onClick = onNavigateToUrunEkle,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Ekle", tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Kataloğa Yeni Ürün Ekle", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToSiparisler,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ListAlt, contentDescription = "Siparişler", tint = Color(0xFF1E293B), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Gelen Siparişleri Yönet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToKatalog, // Bunu parametre olarak ekleyeceğiz
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Inventory, contentDescription = "Katalog", tint = Color(0xFF2563EB), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Kataloğumu Yönet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
            }
        }
    }
}

@Composable
fun DashboardCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color, bgColor: Color) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(28.dp))
            Column {
                Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color.copy(alpha = 0.8f))
            }
        }
    }
}