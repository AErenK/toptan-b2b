package com.example.toptan.ui.musteri

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.model.Siparis
import com.example.toptan.viewmodel.musteri.MusteriSiparisViewModel
import com.example.toptan.viewmodel.musteri.CartViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.toptan.utils.PdfHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: MusteriSiparisViewModel = viewModel(),
    cartViewModel: CartViewModel,
    onNavigateToCart: () -> Unit
) {
    val siparisler by viewModel.siparisler.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()
    val siparisMesaji by cartViewModel.siparisMesaji.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(siparisMesaji) {
        siparisMesaji?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            cartViewModel.mesajiTemizle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fatura & Siparişlerim", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                        if (siparisler.isNotEmpty()) {
                            Text("${siparisler.size} sipariş kaydı bulundu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), Color.Transparent))))

            if (yukleniyor) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            } else if (siparisler.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Box(modifier = Modifier.size(90.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), shape = CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = "Boş", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f), modifier = Modifier.size(42.dp))
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text("Henüz sipariş vermediniz", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                ) {
                    items(siparisler.size) { index ->
                        MusteriSiparisKarti(
                            siparis = siparisler[index],
                            onTekrarlaClick = {
                                cartViewModel.gecmisSiparisiTekrarla(
                                    siparisOzeti = siparisler[index].siparisOzeti,
                                    toptanciId = siparisler[index].toptanciId,
                                    onSuccess = onNavigateToCart
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MusteriSiparisKarti(siparis: Siparis, onTekrarlaClick: () -> Unit) {
    val formatliTutar = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(siparis.toplamTutar)
    val tarihTemsili = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("tr-TR")).format(Date(siparis.tarih))
    val context = LocalContext.current

    // YENİ: Renkler karanlık mod için alfa (saydamlık) değerleriyle daha dinamik hale getirildi
    val (durumRengi, arkaPlanRenk, durumIkonu) = when (siparis.durum) {
        "Hazırlanıyor" -> Triple(Color(0xFF3B82F6), Color(0xFF3B82F6).copy(alpha = 0.15f), Icons.Default.Schedule)
        "Yola Çıktı" -> Triple(Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f), Icons.Default.LocalShipping)
        "Teslim Edildi" -> Triple(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f), Icons.Default.CheckCircle)
        "İptal Edildi" -> Triple(Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f), Icons.Default.Cancel)
        else -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), Icons.Default.CheckCircle)
    }

    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = tarihTemsili, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Box(modifier = Modifier.background(arkaPlanRenk, shape = RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(durumIkonu, contentDescription = null, tint = durumRengi, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = siparis.durum, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = durumRengi)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            SiparisDurumCizelgesi(siparis.durum)

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            if (siparis.sirketUnvani.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Fatura Kesilen: ${siparis.sirketUnvani}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(text = "Sipariş İçeriği", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = siparis.siparisOzeti, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = "Toplam Tutar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                    Text(
                        text = "$formatliTutar ₺", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp,
                        color = if (siparis.durum == "İptal Edildi") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
                        textDecoration = if (siparis.durum == "İptal Edildi") TextDecoration.LineThrough else null
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { PdfHelper.siparisPdfOlustur(context, siparis) },
                        modifier = Modifier.size(36.dp).background(Color(0xFFEF4444).copy(alpha = 0.1f), CircleShape)
                    ) { Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp)) }

                    if (siparis.durum != "İptal Edildi") {
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = onTekrarlaClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tekrarla", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SiparisDurumCizelgesi(durum: String) {
    val adimlar = listOf("Sipariş\nAlındı", "Kargoya\nVerildi", "Teslim\nEdildi")
    val aktifAdim = when (durum) {
        "Yeni", "Hazırlanıyor" -> 0
        "Yola Çıktı" -> 1
        "Teslim Edildi" -> 2
        else -> -1
    }

    if (durum == "İptal Edildi") {
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFEF4444).copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sipariş iptal edildi. Tutar açık hesabınıza iade edilmiştir.", fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Medium)
        }
    } else {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            adimlar.forEachIndexed { index, adim ->
                val isCompleted = index <= aktifAdim
                val isCurrent = index == aktifAdim
                val renk = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Box(modifier = Modifier.size(24.dp).background(renk, CircleShape), contentAlignment = Alignment.Center) {
                        if (isCompleted && !isCurrent) Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        else if (isCurrent) Box(modifier = Modifier.size(10.dp).background(Color.White, CircleShape))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = adim,
                        fontSize = 10.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }

                if (index < adimlar.size - 1) {
                    HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 4.dp).offset(y = (-12).dp), color = if (index < aktifAdim) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 2.dp)
                }
            }
        }
    }
}