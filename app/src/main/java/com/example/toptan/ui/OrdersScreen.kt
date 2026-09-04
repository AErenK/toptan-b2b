package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
fun OrdersScreen(viewModel: MusteriSiparisViewModel = viewModel()) {
    val siparisler by viewModel.gecmisSiparisler.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Siparişlerim", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (yukleniyor) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2563EB))
            } else if (siparisler.isEmpty()) {

                // YENİ: Şık Boş Durum (Empty State) Tasarımı
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = "Sipariş Yok",
                        tint = Color.LightGray,
                        modifier = Modifier.size(120.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz sipariş vermediniz",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Katalogdan ürün seçerek ilk toptan siparişinizi hemen oluşturabilirsiniz.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
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

    val (durumRengi, arkaPlanRenk) = when (siparis.durum) {
        "Yola Çıktı" -> Pair(Color(0xFFD97706), Color(0xFFFEF3C7))
        "Teslim Edildi" -> Pair(Color(0xFF16A34A), Color(0xFFDCFCE7))
        else -> Pair(Color(0xFF2563EB), Color(0xFFDBEAFE)) // Hazırlanıyor veya diğer durumlar
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Sipariş Kodu: ${siparis.siparisId.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                Text(text = tarihTemsili, fontSize = 12.sp, color = Color(0xFF94A3B8))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "İçerik:", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = siparis.siparisOzeti, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color(0xFF334155))

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Toplam Tutar", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(text = "$formatliTutar ₺", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF16A34A))
                }

                Box(
                    modifier = Modifier
                        .background(arkaPlanRenk, shape = RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Durum", tint = durumRengi, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = siparis.durum, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = durumRengi)
                    }
                }
            }
        }
    }
}