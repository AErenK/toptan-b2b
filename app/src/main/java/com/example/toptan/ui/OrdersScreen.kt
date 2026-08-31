package com.example.toptan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.viewmodel.OrdersViewModel
import com.example.toptan.viewmodel.Siparis
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(viewModel: OrdersViewModel = viewModel()) {
    val siparisler by viewModel.siparisler.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Geçmiş Siparişlerim", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // 1. Durum: Veriler yükleniyor
            if (yukleniyor) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // 2. Durum: Liste boş (Henüz sipariş yok)
            else if (siparisler.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = "Boş",
                        modifier = Modifier.size(72.dp),
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Henüz bir siparişiniz bulunmuyor.", color = Color.Gray, fontSize = 16.sp)
                }
            }
            // 3. Durum: Siparişler listeleniyor
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(siparisler.size) { index ->
                        OrderItemCard(siparis = siparisler[index])
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun OrderItemCard(siparis: Siparis) {
    val formatliTutar = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(siparis.toplamTutar)

    // Siparişin durumuna göre etiket rengini belirliyoruz
    val durumRengi = when(siparis.durum) {
        "Hazırlanıyor" -> Color(0xFFF57C00) // Turuncu
        "Yola Çıktı" -> Color(0xFF1976D2)   // Mavi
        "Teslim Edildi" -> Color(0xFF388E3C) // Yeşil
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Kısım: Tarih ve Durum Etiketi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = siparis.formatliTarih, fontSize = 14.sp, color = Color.Gray)

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = durumRengi.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = siparis.durum,
                        color = durumRengi,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Orta Kısım: Sipariş Özeti
            Text(text = "Ürünler:", fontSize = 12.sp, color = Color.Gray)
            Text(text = siparis.siparisOzeti, fontSize = 14.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Alt Kısım: Kargo ve Tutar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = "Kargo",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Standart Teslimat", fontSize = 12.sp, color = Color.Gray)
                }

                Text(
                    text = "$formatliTutar ₺",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1565C0)
                )
            }
        }
    }
}