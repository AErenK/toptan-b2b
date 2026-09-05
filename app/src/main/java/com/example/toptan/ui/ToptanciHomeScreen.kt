package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.MonetizationOn
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
    onNavigateToKatalog: () -> Unit
) {
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
                title = {
                    Column {
                        Text(
                            text = "Toptancı Paneli",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "Yönetim ve İstatistik Ekranı",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC)),
                actions = {
                    IconButton(
                        onClick = { authViewModel.cikisYap(); onLogoutClick() },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(38.dp)
                            .background(Color(0xFFFEF2F2), shape = CircleShape)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Arka plana şık bir gradyan derinliği
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2563EB).copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // --- 1. İSTATİSTİK KARTLARI ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
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

                Spacer(modifier = Modifier.height(14.dp))

                // Ciro Kartı (Geniş ve Premium)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16A34A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Toplam Ciro (Teslim Edilen)", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$formatliCiro ₺", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = "Ciro", tint = Color.White, modifier = Modifier.size(26.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                Spacer(modifier = Modifier.height(24.dp))

                // --- 2. HIZLI İŞLEM BUTONLARI (Zarif ve Akıcı Tasarım) ---
                ToptanciActionCard(
                    title = "Kataloğa Yeni Ürün Ekle",
                    subtitle = "Yeni toptan ürünler yükle ve stok belirle",
                    icon = Icons.Default.AddCircle,
                    iconTint = Color.White,
                    iconBgColor = Color(0xFF1E293B),
                    containerColor = Color(0xFF1E293B),
                    textColor = Color.White,
                    subtitleColor = Color(0xFF94A3B8),
                    onClick = onNavigateToUrunEkle
                )

                Spacer(modifier = Modifier.height(12.dp))

                ToptanciActionCard(
                    title = "Gelen Siparişleri Yönet",
                    subtitle = "Müşteri sipariş durumlarını güncelle",
                    icon = Icons.Default.ListAlt,
                    iconTint = Color(0xFF2563EB),
                    iconBgColor = Color(0xFFDBEAFE),
                    containerColor = Color.White,
                    textColor = Color(0xFF1E293B),
                    subtitleColor = Color(0xFF64748B),
                    onClick = onNavigateToSiparisler
                )

                Spacer(modifier = Modifier.height(12.dp))

                ToptanciActionCard(
                    title = "Kataloğumu Yönet",
                    subtitle = "Mevcut ürünleri incele ve düzenle",
                    icon = Icons.Default.Inventory,
                    iconTint = Color(0xFF2563EB),
                    iconBgColor = Color(0xFFDBEAFE),
                    containerColor = Color.White,
                    textColor = Color(0xFF1E293B),
                    subtitleColor = Color(0xFF64748B),
                    onClick = onNavigateToKatalog
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DashboardCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color, bgColor: Color) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(color.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ToptanciActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    containerColor: Color,
    textColor: Color,
    subtitleColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(iconBgColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = subtitleColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Git",
                tint = subtitleColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}