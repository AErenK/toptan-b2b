package com.example.toptan.ui.toptanci

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalShipping
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
                title = {
                    Column {
                        Text(
                            text = "Gelen Siparişler",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF1E293B)
                        )
                        if (siparisler.isNotEmpty()) {
                            Text(
                                text = "${siparisler.size} aktif sipariş",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(38.dp)
                            .background(Color(0xFFF1F5F9), shape = CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
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
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "Boş Sipariş Kutusu",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(42.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text("Henüz siparişiniz yok", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Müşterilerinizden gelen yeni siparişler anlık olarak burada listelenecektir.",
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
    onDurumDegistir: (String, String) -> Unit
) {
    val formatliTutar = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(siparis.toplamTutar)
    val tarihFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
    val tarihTemsili = tarihFormat.format(Date(siparis.tarih))

    val (durumRengi, arkaPlanRenk) = when (siparis.durum) {
        "Yola Çıktı" -> Pair(Color(0xFFD97706), Color(0xFFFEF3C7))
        "Teslim Edildi" -> Pair(Color(0xFF16A34A), Color(0xFFDCFCE7))
        else -> Pair(Color(0xFF2563EB), Color(0xFFDBEAFE))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Kısım: Müşteri Bilgisi ve Tarih
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = siparis.musteriEmail,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1
                )
                Text(
                    text = tarihTemsili,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

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
                color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Alt Kısım: Toplam Tutar ve Durum Rozeti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Toplam Tutar",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$formatliTutar ₺",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Color(0xFF16A34A)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(arkaPlanRenk, shape = RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CheckCircle,
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

            // Aksiyon Butonları
            if (siparis.durum == "Hazırlanıyor") {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { onDurumDegistir(siparis.siparisId, "Yola Çıktı") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        Icons.Default.LocalShipping,
                        contentDescription = "Kargo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Kargoya Verildi Olarak İşaretle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else if (siparis.durum == "Yola Çıktı") {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { onDurumDegistir(siparis.siparisId, "Teslim Edildi") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Teslim",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Teslim Edildi Olarak İşaretle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}