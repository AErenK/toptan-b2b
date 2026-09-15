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
                title = { Text("Müşteri & Cari Yönetimi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = MaterialTheme.colorScheme.onBackground) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth()) {
                OutlinedTextField(
                    value = aramaMetni, onValueChange = { aramaMetni = it },
                    placeholder = { Text("Şirket Adı veya Yetkili Ara...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            AnimatedVisibility(visible = !mesaj.isNullOrEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().background(if (mesaj.orEmpty().contains("başarılı")) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)).padding(12.dp), contentAlignment = Alignment.Center) {
                    Text(text = mesaj?.replace(" (başarılı)", "") ?: "", color = if (mesaj.orEmpty().contains("başarılı")) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            if (yukleniyor) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            } else if (filtrelenmisMusteriler.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Gösterilecek müşteri bulunamadı.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)) }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxSize()) {
                    items(filtrelenmisMusteriler) { musteri ->
                        MusteriKarti(
                            musteri = musteri,
                            onLimitDuzenleClick = { seciliMusteri = musteri; yeniLimitMetni = musteri.cariLimit.toInt().toString() },
                            onTahsilatClick = { tahsilatMusteri = musteri; tahsilatTutari = ""; tahsilatAciklama = "" },
                            onIskontoClick = { iskontoMusteri = musteri; yeniIskontoMetni = musteri.iskontoOrani.toString() },
                            onEkstreClick = {
                                ekstreMusteri = musteri
                                viewModel.musteriHareketleriniGetir(musteri.uid)
                            }
                        )
                    }
                }
            }
        }
    }

    if (ekstreMusteri != null) {
        AlertDialog(
            onDismissRequest = { ekstreMusteri = null },
            title = { Text("Hesap Ekstresi - ${ekstreMusteri!!.sirketUnvani}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                if (musteriHareketleri.isEmpty()) {
                    Text("Bu müşteriye ait henüz bir hesap hareketi (tahsilat vb.) bulunmuyor.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 13.sp)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        items(musteriHareketleri) { hareket ->
                            val formatliTutar = NumberFormat.getNumberInstance(Locale("tr","TR")).format(hareket.tutar)
                            val tarih = SimpleDateFormat("dd MMM yy HH:mm", Locale("tr","TR")).format(Date(hareket.tarih))

                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(hareket.islemTuru, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 14.sp)
                                    Text("+$formatliTutar ₺", fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981), fontSize = 14.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(hareket.aciklama, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    Text(tarih, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { Button(onClick = { ekstreMusteri = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Kapat", color = MaterialTheme.colorScheme.background) } }
        )
    }

    if (seciliMusteri != null) {
        AlertDialog(onDismissRequest = { seciliMusteri = null }, title = { Text("Cari Limit Düzenle", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { OutlinedTextField(value = yeniLimitMetni, onValueChange = { yeniLimitMetni = it }, label = { Text("Yeni Limit Sınırı (₺)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { Button(onClick = { viewModel.cariLimitGuncelle(seciliMusteri!!.uid, yeniLimitMetni.toDoubleOrNull() ?: 0.0); seciliMusteri = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Kaydet", color = MaterialTheme.colorScheme.background) } },
            dismissButton = { TextButton(onClick = { seciliMusteri = null }) { Text("İptal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) } }
        )
    }
    if (tahsilatMusteri != null) {
        AlertDialog(onDismissRequest = { tahsilatMusteri = null }, title = { Text("Tahsilat Al", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Column {
                OutlinedTextField(value = tahsilatTutari, onValueChange = { tahsilatTutari = it }, label = { Text("Tutar (₺)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = tahsilatAciklama, onValueChange = { tahsilatAciklama = it }, label = { Text("Açıklama (Örn: Havale)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            } },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { Button(onClick = { viewModel.tahsilatEkle(tahsilatMusteri!!.uid, tahsilatTutari.toDoubleOrNull() ?: 0.0, if (tahsilatAciklama.isNotBlank()) tahsilatAciklama else "Nakit Tahsilat"); tahsilatMusteri = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Kaydet", color = MaterialTheme.colorScheme.background) } },
            dismissButton = { TextButton(onClick = { tahsilatMusteri = null }) { Text("İptal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) } }
        )
    }
    if (iskontoMusteri != null) {
        AlertDialog(onDismissRequest = { iskontoMusteri = null }, title = { Text("Özel İskonto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { OutlinedTextField(value = yeniIskontoMetni, onValueChange = { yeniIskontoMetni = it }, label = { Text("İndirim Oranı (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = { Button(onClick = { viewModel.iskontoGuncelle(iskontoMusteri!!.uid, yeniIskontoMetni.toDoubleOrNull() ?: 0.0); iskontoMusteri = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Uygula", color = MaterialTheme.colorScheme.background) } },
            dismissButton = { TextButton(onClick = { iskontoMusteri = null }) { Text("İptal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) } }
        )
    }
}

@Composable
fun MusteriKarti(musteri: Musteri, onLimitDuzenleClick: () -> Unit, onTahsilatClick: () -> Unit, onIskontoClick: () -> Unit, onEkstreClick: () -> Unit) {
    val formatliLimit = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(musteri.cariLimit)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(46.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(24.dp)) }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = musteri.sirketUnvani, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = musteri.yetkiliKisi, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                        }
                    }
                }
                if (musteri.iskontoOrani > 0.0) {
                    Box(modifier = Modifier.background(Color(0xFF9333EA).copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("%${musteri.iskontoOrani.toInt()} İskonto", color = Color(0xFFC084FC), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
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
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Kullanılabilir Bakiye", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.SemiBold)
                    Text("$formatliLimit ₺", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onEkstreClick, modifier = Modifier.size(34.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), RoundedCornerShape(8.dp))) {
                        Icon(Icons.Default.ListAlt, contentDescription = "Ekstre", tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onIskontoClick, modifier = Modifier.size(34.dp).background(Color(0xFFF59E0B).copy(alpha = 0.15f), RoundedCornerShape(8.dp))) {
                        Icon(Icons.Default.LocalOffer, contentDescription = "İskonto", tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onLimitDuzenleClick, modifier = Modifier.size(34.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))) {
                        Icon(Icons.Default.CreditScore, contentDescription = "Limit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Button(onClick = onTahsilatClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(34.dp)) {
                        Text("Tahsilat", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), lineHeight = 16.sp)
    }
}