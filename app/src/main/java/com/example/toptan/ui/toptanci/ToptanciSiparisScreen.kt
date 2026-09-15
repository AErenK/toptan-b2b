package com.example.toptan.ui.toptanci

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.model.Siparis
import com.example.toptan.utils.PdfHelper
import com.example.toptan.viewmodel.toptanci.ToptanciViewModel
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
    val tumSiparisler by viewModel.toptanciSiparisleri.collectAsState(initial = emptyList())
    val yukleniyor by viewModel.siparislerYukleniyor.collectAsState(initial = false)
    val mesaj by viewModel.mesaj.collectAsState()
    val context = LocalContext.current

    val sekmeler = listOf("Hazırlanıyor", "Yola Çıkanlar", "Tamamlanan/İptal")
    val pagerState = rememberPagerState(pageCount = { sekmeler.size })
    val coroutineScope = rememberCoroutineScope()
    var iptalEdilecekSiparis by remember { mutableStateOf<Siparis?>(null) }

    LaunchedEffect(mesaj) {
        mesaj?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.mesajiTemizle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sipariş Yönetimi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = MaterialTheme.colorScheme.onBackground) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        height = 4.dp
                    )
                }
            ) {
                sekmeler.forEachIndexed { index, baslik ->
                    val isSelected = pagerState.currentPage == index
                    val textColor by animateColorAsState(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), label = "tabColor")
                    Tab(
                        selected = isSelected,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(text = baslik, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = textColor, fontSize = 12.sp) }
                    )
                }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val filtrelenmisSiparisler = when (page) {
                    0 -> tumSiparisler.filter { it.durum == "Hazırlanıyor" || it.durum == "Yeni" }
                    1 -> tumSiparisler.filter { it.durum == "Yola Çıktı" }
                    else -> tumSiparisler.filter { it.durum == "Teslim Edildi" || it.durum == "İptal Edildi" }
                }

                if (yukleniyor) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (filtrelenmisSiparisler.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Bu aşamada sipariş bulunmuyor.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
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
                                onDurumDegistir = { yeniDurum -> viewModel.siparisDurumuGuncelle(siparis.siparisId, yeniDurum) },
                                onIptalEt = { iptalEdilecekSiparis = siparis }
                            )
                        }
                    }
                }
            }
        }
    }

    if (iptalEdilecekSiparis != null) {
        val formatliTutar = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(iptalEdilecekSiparis!!.toplamTutar)
        AlertDialog(
            onDismissRequest = { iptalEdilecekSiparis = null },
            title = { Text("Siparişi İptal Et", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 18.sp) },
            text = {
                Text(
                    "Bu siparişi iptal etmek istediğinize emin misiniz?\n\nİptal ederseniz sipariş tutarı olan $formatliTutar ₺ müşterinin Cari Limitine anında geri yüklenecektir.",
                    fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.siparisIptalEtVeIadeYap(iptalEdilecekSiparis!!)
                        iptalEdilecekSiparis = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Evet, İptal ve İade Et", fontWeight = FontWeight.Bold, color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { iptalEdilecekSiparis = null }) { Text("Vazgeç", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
fun ToptanciSiparisKarti(siparis: Siparis, onDurumDegistir: (String) -> Unit, onIptalEt: () -> Unit) {
    val formatliTutar = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(siparis.toplamTutar)
    val tarihFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("tr-TR"))
    val tarihTemsili = tarihFormat.format(Date(siparis.tarih))
    val context = LocalContext.current

    val (durumRengi, arkaPlanRenk, durumIkonu) = when (siparis.durum) {
        "Hazırlanıyor", "Yeni" -> Triple(Color(0xFF3B82F6), Color(0xFF3B82F6).copy(alpha = 0.15f), Icons.Default.Schedule)
        "Yola Çıktı" -> Triple(Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f), Icons.Default.LocalShipping)
        "Teslim Edildi" -> Triple(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f), Icons.Default.CheckCircle)
        "İptal Edildi" -> Triple(Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f), Icons.Default.Cancel)
        else -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), Icons.Default.CheckCircle)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = tarihTemsili, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Box(modifier = Modifier.background(arkaPlanRenk, shape = RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(durumIkonu, contentDescription = "Durum", tint = durumRengi, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = siparis.durum, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = durumRengi)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = siparis.sirketUnvani, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${siparis.yetkiliKisi} - ${siparis.telefon}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(text = siparis.siparisOzeti, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 18.sp, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Sipariş Tutarı", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(
                        text = "$formatliTutar ₺", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
                        textDecoration = if (siparis.durum == "İptal Edildi") androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                        color = if (siparis.durum == "İptal Edildi") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (siparis.durum != "İptal Edildi") {
                        IconButton(
                            onClick = { PdfHelper.siparisPdfOlustur(context, siparis) },
                            modifier = Modifier.size(36.dp).background(Color(0xFFEF4444).copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                        when (siparis.durum) {
                            "Hazırlanıyor", "Yeni" -> {
                                IconButton(
                                    onClick = onIptalEt,
                                    modifier = Modifier.size(36.dp).background(Color(0xFFEF4444).copy(alpha = 0.1f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Cancel, contentDescription = "İptal Et", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                                Button(
                                    onClick = { onDurumDegistir("Yola Çıktı") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kargoya Ver", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            "Yola Çıktı" -> {
                                Button(
                                    onClick = { onDurumDegistir("Teslim Edildi") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Teslim Edildi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}