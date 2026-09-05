package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
                title = { Text("Siparişlerim", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (yukleniyor) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2563EB))
            } else if (siparisler.isEmpty()) {
                // Şık Boş Durum Tasarımı
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Boş Sipariş",
                        tint = Color.LightGray,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Henüz sipariş vermediniz", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Katalogdan ürün seçip hemen ilk toptan siparişinizi oluşturabilirsiniz.",
                        fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
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

    // Duruma göre renkler ve ikonlar
    val (durumRengi, arkaPlanRenk, durumIkonu) = when (siparis.durum) {
        "Hazırlanıyor" -> Triple(Color(0xFF2563EB), Color(0xFFDBEAFE), Icons.Default.Schedule)
        "Yola Çıktı" -> Triple(Color(0xFFD97706), Color(0xFFFEF3C7), Icons.Default.LocalShipping)
        "Teslim Edildi" -> Triple(Color(0xFF16A34A), Color(0xFFDCFCE7), Icons.Default.CheckCircle)
        else -> Triple(Color.Gray, Color(0xFFF1F5F9), Icons.Default.CheckCircle)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Üst Kısım: Tarih ve Durum Etiketi
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = tarihTemsili, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))

                Box(
                    modifier = Modifier.background(arkaPlanRenk, shape = RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(durumIkonu, contentDescription = "Durum", tint = durumRengi, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = siparis.durum, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = durumRengi)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            // Orta Kısım: Sipariş Özeti
            Text(text = "Sipariş İçeriği", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = siparis.siparisOzeti, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color(0xFF1E293B))

            Spacer(modifier = Modifier.height(16.dp))

            // Alt Kısım: Toplam Tutar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Toplam: ", fontSize = 14.sp, color = Color(0xFF64748B))
                Text(text = "$formatliTutar ₺", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E293B))
            }
        }
    }
}