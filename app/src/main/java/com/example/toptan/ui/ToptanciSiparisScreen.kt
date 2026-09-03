package com.example.toptan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
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
import com.example.toptan.model.Siparis
import com.example.toptan.viewmodel.ToptanciSiparisViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToptanciSiparisScreen(
    viewModel: ToptanciSiparisViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val siparisler by viewModel.gelenSiparisler.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gelen Siparişler", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0))
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (yukleniyor) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (siparisler.isEmpty()) {
                Text("Henüz gelen bir siparişiniz yok.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(siparisler.size) { index ->
                        // ViewModel'deki güncelleme fonksiyonunu karta iletiyoruz
                        SiparisKarti(
                            siparis = siparisler[index],
                            onDurumDegistir = { siparisId, yeniDurum ->
                                viewModel.siparisDurumuGuncelle(siparisId, yeniDurum)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SiparisKarti(
    siparis: Siparis,
    onDurumDegistir: (String, String) -> Unit // YENİ PARAMETRE
) {
    val formatliTutar = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(siparis.toplamTutar)
    val tarihFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
    val tarihTemsili = tarihFormat.format(Date(siparis.tarih))

    // Duruma göre ikon ve renk belirleme
    val durumRengi = when (siparis.durum) {
        "Yola Çıktı" -> Color(0xFFF57C00) // Turuncu
        "Teslim Edildi" -> Color(0xFF2E7D32) // Yeşil
        else -> Color(0xFF1565C0) // Mavi (Hazırlanıyor)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Müşteri: ${siparis.musteriEmail}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = tarihTemsili, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Sipariş İçeriği:", fontSize = 12.sp, color = Color.Gray)
            Text(text = siparis.siparisOzeti, fontWeight = FontWeight.Medium, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Toplam Tutar", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "$formatliTutar ₺", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Durum", tint = durumRengi, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = siparis.durum, fontWeight = FontWeight.Bold, color = durumRengi)
                }
            }

            // DURUM GÜNCELLEME BUTONLARI
            if (siparis.durum == "Hazırlanıyor") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onDurumDegistir(siparis.siparisId, "Yola Çıktı") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = "Kargo", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kargoya Verildi Olarak İşaretle", fontWeight = FontWeight.Bold)
                }
            } else if (siparis.durum == "Yola Çıktı") {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { onDurumDegistir(siparis.siparisId, "Teslim Edildi") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Teslim", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Teslim Edildi Olarak İşaretle", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}