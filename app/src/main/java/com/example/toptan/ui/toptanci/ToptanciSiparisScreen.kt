package com.example.toptan.ui.toptanci

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.model.Siparis
import com.example.toptan.viewmodel.ToptanciViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ToptanciSiparisScreen(
    viewModel: ToptanciViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    // Tüm siparişleri ViewModel'den dinliyoruz
    val tumSiparisler by viewModel.toptanciSiparisleri.collectAsState(initial = emptyList())
    val yukleniyor by viewModel.siparislerYukleniyor.collectAsState(initial = false)

    // Sekmeler (Tabs)
    val sekmeler = listOf("Hazırlanıyor", "Yola Çıkanlar", "Tamamlandı")

    // Pager (Sağa Sola Kaydırma Hareketi İçin)
    val pagerState = rememberPagerState(pageCount = { sekmeler.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sipariş Yönetimi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color(0xFF1E293B)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- TAB ROW (SEKMELER) ---
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.White,
                contentColor = Color(0xFF2563EB),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        color = Color(0xFF2563EB),
                        height = 4.dp
                    )
                }
            ) {
                sekmeler.forEachIndexed { index, baslik ->
                    val isSelected = pagerState.currentPage == index
                    val textColor by animateColorAsState(if (isSelected) Color(0xFF2563EB) else Color(0xFF64748B), label = "tabColor")

                    Tab(
                        selected = isSelected,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(text = baslik, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = textColor) }
                    )
                }
            }

            // --- PAGER İÇERİĞİ (SİPARİŞ LİSTELERİ) ---
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val filtrelenmisSiparisler = when (page) {
                    0 -> tumSiparisler.filter { it.durum == "Hazırlanıyor" || it.durum == "Yeni" }
                    1 -> tumSiparisler.filter { it.durum == "Yola Çıktı" }
                    else -> tumSiparisler.filter { it.durum == "Teslim Edildi" || it.durum == "İptal" }
                }

                if (yukleniyor) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                    }
                } else if (filtrelenmisSiparisler.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Bu aşamada sipariş bulunmuyor.", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filtrelenmisSiparisler) { siparis ->
                            ToptanciSiparisKarti(
                                siparis = siparis,
                                onDurumDegistir = { yeniDurum ->
                                    viewModel.siparisDurumuGuncelle(siparis.siparisId, yeniDurum)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToptanciSiparisKarti(siparis: Siparis, onDurumDegistir: (String) -> Unit) {
    val formatliTutar = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(siparis.toplamTutar)
    val tarihFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("tr-TR"))
    val tarihTemsili = tarihFormat.format(Date(siparis.tarih))

    // Duruma göre görsel renkler
    val (durumRengi, arkaPlanRenk, durumIkonu) = when (siparis.durum) {
        "Hazırlanıyor", "Yeni" -> Triple(Color(0xFF2563EB), Color(0xFFDBEAFE), Icons.Default.Schedule)
        "Yola Çıktı" -> Triple(Color(0xFFD97706), Color(0xFFFEF3C7), Icons.Default.LocalShipping)
        "Teslim Edildi" -> Triple(Color(0xFF16A34A), Color(0xFFDCFCE7), Icons.Default.CheckCircle)
        else -> Triple(Color(0xFF64748B), Color(0xFFF1F5F9), Icons.Default.CheckCircle)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. ÜST: Tarih ve Durum Rozeti
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = tarihTemsili, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B))
                Box(modifier = Modifier.background(arkaPlanRenk, shape = RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(durumIkonu, contentDescription = "Durum", tint = durumRengi, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = siparis.durum, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = durumRengi)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // 2. ORTA: Müşteri ve Sipariş Detayları
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(Color(0xFFF8FAFC), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = siparis.sirketUnvani, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF1E293B))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${siparis.yetkiliKisi} - ${siparis.telefon}", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(color = Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = siparis.siparisOzeti,
                    fontSize = 13.sp, color = Color(0xFF475569), lineHeight = 18.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. ALT: Toplam Tutar ve AKSİYON BUTONU
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Sipariş Tutarı", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text("$formatliTutar ₺", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF2563EB))
                }

                // Siparişin durumuna göre dinamik buton gösterimi
                when (siparis.durum) {
                    "Hazırlanıyor", "Yeni" -> {
                        Button(
                            onClick = { onDurumDegistir("Yola Çıktı") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kargoya Ver", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "Yola Çıktı" -> {
                        Button(
                            onClick = { onDurumDegistir("Teslim Edildi") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Teslim Edildi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}