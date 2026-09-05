package com.example.toptan.ui.musteri

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.model.Siparis
import com.example.toptan.viewmodel.MusteriSiparisViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: MusteriSiparisViewModel = viewModel()
) {
    val siparisler by viewModel.siparisler.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Siparişlerim",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        if (siparisler.isNotEmpty()) {
                            Text(
                                text = "${siparisler.size} toplam sipariş",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Arka plana hafif tasarım derinliği (Degrade)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2563EB).copy(alpha = 0.04f), Color.Transparent)
                        )
                    )
            )

            if (yukleniyor) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else if (siparisler.isEmpty()) {
                // Şık Boş Durum Tasarımı
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(Color(0xFFE2E8F0), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = "Boş Sipariş",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(42.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text("Henüz sipariş vermediniz", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Katalogdan ürün seçip hemen ilk toptan siparişinizi oluşturabilirsiniz.",
                            fontSize = 13.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    items(siparisler.size) { index ->
                        MusteriSiparisKarti(siparis = siparisler[index])
                    }
                }
            }
        }
    }
}

@Composable
fun MusteriSiparisKarti(siparis: Siparis) {
    val formatliTutar = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(siparis.toplamTutar)
    val tarihFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
    val tarihTemsili = tarihFormat.format(Date(siparis.tarih))

    // Duruma göre profesyonel renkler, arka planlar ve ikonlar
    val (durumRengi, arkaPlanRenk, durumIkonu) = when (siparis.durum) {
        "Hazırlanıyor" -> Triple(Color(0xFF2563EB), Color(0xFFDBEAFE), Icons.Default.Schedule)
        "Yola Çıktı" -> Triple(Color(0xFFD97706), Color(0xFFFEF3C7), Icons.Default.LocalShipping)
        "Teslim Edildi" -> Triple(Color(0xFF16A34A), Color(0xFFDCFCE7), Icons.Default.CheckCircle)
        else -> Triple(Color(0xFF64748B), Color(0xFFF1F5F9), Icons.Default.CheckCircle)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Kısım: Tarih ve Durum Etiketi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tarihTemsili,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )

                Box(
                    modifier = Modifier
                        .background(arkaPlanRenk, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            durumIkonu,
                            contentDescription = "Durum",
                            tint = durumRengi,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = siparis.durum,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = durumRengi
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Orta Kısım: Sipariş Özeti
            Text(
                text = "Sipariş İçeriği",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = siparis.siparisOzeti,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Alt Kısım: Toplam Tutar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Toplam Tutar",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$formatliTutar ₺",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF2563EB)
                )
            }
        }
    }
}