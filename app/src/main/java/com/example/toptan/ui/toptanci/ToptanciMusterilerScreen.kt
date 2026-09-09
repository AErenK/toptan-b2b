package com.example.toptan.ui.toptanci

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.viewmodel.Musteri
import com.example.toptan.viewmodel.ToptanciMusterilerViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToptanciMusterilerScreen(
    viewModel: ToptanciMusterilerViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val musteriler by viewModel.musteriler.collectAsState()
    val yukleniyor by viewModel.yukleniyor.collectAsState()
    val mesaj by viewModel.mesaj.collectAsState()

    var aramaMetni by remember { mutableStateOf("") }

    // Limit güncelleme penceresi için state'ler
    var seciliMusteri by remember { mutableStateOf<Musteri?>(null) }
    var yeniLimitMetni by remember { mutableStateOf("") }

    // Arama filtrelemesi
    val filtrelenmisMusteriler = musteriler.filter {
        it.sirketUnvani.contains(aramaMetni, ignoreCase = true) ||
                it.yetkiliKisi.contains(aramaMetni, ignoreCase = true)
    }

    // Mesajları Toast/Snackbar gibi göstermek için (Basit tutuldu)
    LaunchedEffect(mesaj) {
        if (mesaj != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.mesajiTemizle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Müşteri Yönetimi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color(0xFF1E293B)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // Arama Çubuğu
            Box(modifier = Modifier.background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth()) {
                OutlinedTextField(
                    value = aramaMetni,
                    onValueChange = { aramaMetni = it },
                    placeholder = { Text("Şirket Adı veya Yetkili Ara...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF1F5F9),
                        unfocusedContainerColor = Color(0xFFF1F5F9)
                    )
                )
            }

            // Mesaj Bildirimi
            AnimatedVisibility(visible = !mesaj.isNullOrEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(if (mesaj.orEmpty().contains("başarılı")) Color(0xFFDCFCE7) else Color(0xFFFEF2F2)).padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = mesaj?.replace(" (başarılı)", "") ?: "", color = if (mesaj.orEmpty().contains("başarılı")) Color(0xFF16A34A) else Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (yukleniyor) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2563EB))
                }
            } else if (filtrelenmisMusteriler.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Gösterilecek müşteri bulunamadı.", color = Color(0xFF64748B))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtrelenmisMusteriler) { musteri ->
                        MusteriKarti(
                            musteri = musteri,
                            onLimitDuzenleClick = {
                                seciliMusteri = musteri
                                yeniLimitMetni = musteri.cariLimit.toInt().toString()
                            }
                        )
                    }
                }
            }
        }
    }

    // YENİ: CARİ LİMİT DÜZENLEME DİALOGU
    if (seciliMusteri != null) {
        AlertDialog(
            onDismissRequest = { seciliMusteri = null },
            title = { Text("Cari Limit Düzenle", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
            text = {
                Column {
                    Text(seciliMusteri!!.sirketUnvani, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    Text("Müşterisi için tanımlanacak yeni açık hesap (cari) limitini belirleyin.", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = yeniLimitMetni,
                        onValueChange = { yeniLimitMetni = it },
                        label = { Text("Yeni Limit (₺)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        val limit = yeniLimitMetni.toDoubleOrNull()
                        if (limit != null && limit >= 0) {
                            viewModel.cariLimitGuncelle(seciliMusteri!!.uid, limit)
                            seciliMusteri = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Kaydet", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { seciliMusteri = null }) { Text("İptal", color = Color(0xFF64748B)) }
            }
        )
    }
}

@Composable
fun MusteriKarti(musteri: Musteri, onLimitDuzenleClick: () -> Unit) {
    val formatliLimit = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(musteri.cariLimit)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Kısım: Şirket Adı ve Yetkili
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(46.dp).background(Color(0xFFF1F5F9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = musteri.sirketUnvani, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = musteri.yetkiliKisi, fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(16.dp))

            // İletişim ve Vergi Bilgileri
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoRow(icon = Icons.Default.Phone, text = musteri.telefon)
                    Spacer(modifier = Modifier.height(6.dp))
                    InfoRow(icon = Icons.Default.Email, text = musteri.email)
                }
                Column(modifier = Modifier.weight(1f)) {
                    InfoRow(icon = Icons.Default.ReceiptLong, text = "${musteri.vergiDairesi}\nVN: ${musteri.vergiNo}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alt Kısım: Limit ve Buton
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tanımlı Cari Limit", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    Text("$formatliLimit ₺", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                }

                Button(
                    onClick = onLimitDuzenleClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CreditScore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Limiti Düzenle", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = Color(0xFF475569), lineHeight = 16.sp)
    }
}