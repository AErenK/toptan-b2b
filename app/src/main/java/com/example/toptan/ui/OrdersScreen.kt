package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Siparişlerim", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5) // Arka planı hafif gri yapıyoruz ki kartlar öne çıksın
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Gerçek veritabanı bağlanana kadar 4 tane örnek sipariş kartı çizdiriyoruz
            items(4) { index ->
                B2BOrderCard(
                    orderNumber = "SIP-2026${1000 + index}",
                    wholesalerName = if (index % 2 == 0) "Marmara Toptan Gıda" else "Ege Tekstil Üretim",
                    totalAmount = "${(index + 1) * 12500} ₺",
                    status = if (index == 0) "Yola Çıktı" else "Hazırlanıyor"
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun B2BOrderCard(
    orderNumber: String,
    wholesalerName: String,
    totalAmount: String,
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Satır: Sipariş No ve Durum
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = orderNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                // Durum rozeti (Badge)
                Box(
                    modifier = Modifier
                        .background(
                            color = if (status == "Yola Çıktı") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status,
                        color = if (status == "Yola Çıktı") Color(0xFF2E7D32) else Color(0xFFEF6C00),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            // Alt Satır: Toptancı Adı, İkon ve Fiyat
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Toptancı", fontSize = 12.sp, color = Color.Gray)
                    Text(text = wholesalerName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalShipping,
                        contentDescription = "Kargo",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = totalAmount, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1565C0))
                }
            }
        }
    }
}