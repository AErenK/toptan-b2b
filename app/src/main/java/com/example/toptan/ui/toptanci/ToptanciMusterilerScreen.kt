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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.model.Musteri
import com.example.toptan.viewmodel.toptanci.ToptanciMusterilerViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
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

    // YENİ: Hesap Ekstresi State'leri
    val musteriHareketleri by viewModel.musteriHareketleri.collectAsState()
    var ekstreMusteri by remember { mutableStateOf<Musteri?>(null) }

    var aramaMetni by remember { mutableStateOf("") }
    var seciliMusteri by remember { mutableStateOf<Musteri?>(null) }
    var yeniLimitMetni by remember { mutableStateOf("") }
    var tahsilatMusteri by remember { mutableStateOf<Musteri?>(null) }
    var tahsilatTutari by remember { mutableStateOf("") }
    var tahsilatAciklama by remember { mutableStateOf("") }
    var iskontoMusteri by remember { mutableStateOf<Musteri?>(null) }
    var yeniIskontoMetni by remember { mutableStateOf("") }

    val filtrelenmisMusteriler = musteriler.filter {
        it.sirketUnvani.contains(aramaMetni, ignoreCase = true) || it.yetkiliKisi.contains(aramaMetni, ignoreCase = true)
    }

    LaunchedEffect(mesaj) {
        if (mesaj != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.mesajiTemizle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Müşteri & Cari Yönetimi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color(0xFF1E293B)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.background(Color.White).padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth()) {
                OutlinedTextField(
                    value = aramaMetni, onValueChange = { aramaMetni = it },
                    placeholder = { Text("Şirket Adı veya Yetkili Ara...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF1F5F9), unfocusedContainerColor = Color(0xFFF1F5F9)
                    )
                )
            }
            AnimatedVisibility(visible = !mesaj.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().background(if (mesaj.orEmpty().contains("başarılı")) Color(0xFFDCFCE7) else Color(0xFFFEF2F2)).padding(12.dp), contentAlignment = Alignment.Center) {
                    Text(text = mesaj?.replace(" (başarılı)", "") ?: "", color = if (mesaj.orEmpty().contains("başarılı")) Color(0xFF16A34A) else Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (yukleniyor) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF2563EB)) }
            } else if (filtrelenmisMusteriler.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Gösterilecek müşteri bulunamadı.", color = Color(0xFF64748B)) }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
                    items(filtrelenmisMusteriler) { musteri ->
                        MusteriKarti(
                            musteri = musteri,
                            onLimitDuzenleClick = { seciliMusteri = musteri; yeniLimitMetni = musteri.cariLimit.toInt().toString() },
                            onTahsilatClick = { tahsilatMusteri = musteri; tahsilatTutari = ""; tahsilatAciklama = "" },
                            onIskontoClick = { iskontoMusteri = musteri; yeniIskontoMetni = musteri.iskontoOrani.toString() },
                            onEkstreClick = { // YENİ: Ekstre butonuna basıldığında
                                ekstreMusteri = musteri
                                viewModel.musteriHareketleriniGetir(musteri.uid)
                            }
                        )
                    }
                }
            }
        }
    }

    // YENİ: HESAP EKSTRESİ DİALOGU
    if (ekstreMusteri != null) {
        AlertDialog(
            onDismissRequest = { ekstreMusteri = null },
            title = { Text("Hesap Ekstresi - ${ekstreMusteri!!.sirketUnvani}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B)) },
            text = {
                if (musteriHareketleri.isEmpty()) {
                    Text("Bu müşteriye ait henüz bir hesap hareketi (tahsilat vb.) bulunmuyor.", color = Color.Gray, fontSize = 13.sp)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        items(musteriHareketleri) { hareket ->
                            val formatliTutar = NumberFormat.getNumberInstance(Locale("tr","TR")).format(hareket.tutar)
                            val tarih = SimpleDateFormat("dd MMM yy HH:mm", Locale("tr","TR")).format(Date(hareket.tarih))

                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(hareket.islemTuru, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), fontSize = 14.sp)
                                    Text("+$formatliTutar ₺", fontWeight = FontWeight.ExtraBold, color = Color(0xFF16A34A), fontSize = 14.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(hareket.aciklama, fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text(tarih, fontSize = 11.sp, color = Color.Gray)
                                }
                                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { ekstreMusteri = null }) { Text("Kapat") } }
        )
    }

    // DİĞER DİALOGLAR (Limit, Tahsilat, İskonto - Aynı kalacak)
    if (seciliMusteri != null) {
        AlertDialog(onDismissRequest = { seciliMusteri = null }, title = { Text("Cari Limit Düzenle", fontWeight = FontWeight.Bold) },
            text = { OutlinedTextField(value = yeniLimitMetni, onValueChange = { yeniLimitMetni = it }, label = { Text("Yeni Limit Sınırı (₺)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) },
            confirmButton = { Button(onClick = { viewModel.cariLimitGuncelle(seciliMusteri!!.uid, yeniLimitMetni.toDoubleOrNull() ?: 0.0); seciliMusteri = null }) { Text("Kaydet") } },
            dismissButton = { TextButton(onClick = { seciliMusteri = null }) { Text("İptal") } }
        )
    }
    if (tahsilatMusteri != null) {
        AlertDialog(onDismissRequest = { tahsilatMusteri = null }, title = { Text("Tahsilat Al", fontWeight = FontWeight.Bold) },
            text = { Column {
                OutlinedTextField(value = tahsilatTutari, onValueChange = { tahsilatTutari = it }, label = { Text("Tutar (₺)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = tahsilatAciklama, onValueChange = { tahsilatAciklama = it }, label = { Text("Açıklama (Örn: Havale)") }, singleLine = true)
            } },
            confirmButton = { Button(onClick = { viewModel.tahsilatEkle(tahsilatMusteri!!.uid, tahsilatTutari.toDoubleOrNull() ?: 0.0, if (tahsilatAciklama.isNotBlank()) tahsilatAciklama else "Nakit Tahsilat"); tahsilatMusteri = null }) { Text("Kaydet") } },
            dismissButton = { TextButton(onClick = { tahsilatMusteri = null }) { Text("İptal") } }
        )
    }
    if (iskontoMusteri != null) {
        AlertDialog(onDismissRequest = { iskontoMusteri = null }, title = { Text("Özel İskonto", fontWeight = FontWeight.Bold) },
            text = { OutlinedTextField(value = yeniIskontoMetni, onValueChange = { yeniIskontoMetni = it }, label = { Text("İndirim Oranı (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true) },
            confirmButton = { Button(onClick = { viewModel.iskontoGuncelle(iskontoMusteri!!.uid, yeniIskontoMetni.toDoubleOrNull() ?: 0.0); iskontoMusteri = null }) { Text("Uygula") } },
            dismissButton = { TextButton(onClick = { iskontoMusteri = null }) { Text("İptal") } }
        )
    }
}

@Composable
fun MusteriKarti(musteri: Musteri, onLimitDuzenleClick: () -> Unit, onTahsilatClick: () -> Unit, onIskontoClick: () -> Unit, onEkstreClick: () -> Unit) {
    val formatliLimit = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(musteri.cariLimit)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(46.dp).background(Color(0xFFF1F5F9), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(24.dp)) }
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
                if (musteri.iskontoOrani > 0.0) {
                    Box(modifier = Modifier.background(Color(0xFFF3E8FF), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("%${musteri.iskontoOrani.toInt()} İskonto", color = Color(0xFF9333EA), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(16.dp))
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
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Kullanılabilir Bakiye", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    Text("$formatliLimit ₺", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // YENİ: Hesap Ekstresi Butonu
                    IconButton(onClick = onEkstreClick, modifier = Modifier.size(34.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))) {
                        Icon(Icons.Default.ListAlt, contentDescription = "Ekstre", tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onIskontoClick, modifier = Modifier.size(34.dp).background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))) {
                        Icon(Icons.Default.LocalOffer, contentDescription = "İskonto", tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onLimitDuzenleClick, modifier = Modifier.size(34.dp).background(Color(0xFFE0F2FE), RoundedCornerShape(8.dp))) {
                        Icon(Icons.Default.CreditScore, contentDescription = "Limit", tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                    }
                    Button(onClick = onTahsilatClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(34.dp)) {
                        Text("Tahsilat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
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