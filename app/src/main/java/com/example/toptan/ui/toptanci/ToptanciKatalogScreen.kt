package com.example.toptan.ui.toptanci

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.toptan.model.Urun
import com.example.toptan.viewmodel.toptanci.ToptanciViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToptanciKatalogScreen(
    viewModel: ToptanciViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val urunler by viewModel.toptanciUrunleri.collectAsState()
    val mesaj by viewModel.mesaj.collectAsState()
    val context = LocalContext.current

    var silinecekUrun by remember { mutableStateOf<Urun?>(null) }

    // Düzenleme Dialog'u için State'ler
    var duzenlenecekUrun by remember { mutableStateOf<Urun?>(null) }
    var yeniFiyatGirdisi by remember { mutableStateOf("") }
    var yeniStokGirdisi by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.toptanciUrunleriniGetir()
    }

    LaunchedEffect(mesaj) {
        mesaj?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.mesajiTemizle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kataloğum", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                        Text("${urunler.size} kayıtlı ürün", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 4.dp).size(38.dp).background(Color(0xFFF1F5F9), shape = CircleShape)
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
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp)
                    .background(Brush.verticalGradient(colors = listOf(Color(0xFF2563EB).copy(alpha = 0.04f), Color.Transparent)))
            )

            if (urunler.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Box(modifier = Modifier.size(90.dp).background(Color(0xFFE2E8F0), shape = CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Inventory, contentDescription = "Boş", tint = Color(0xFF94A3B8), modifier = Modifier.size(42.dp))
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text("Kataloğunuzda henüz ürün yok", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Panel üzerinden yeni toptan ürünler ekleyerek kataloğunuzu zenginleştirebilirsiniz.", fontSize = 13.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(urunler.size) { index ->
                        val urun = urunler[index]
                        KatalogYonetimKarti(
                            urun = urun,
                            onDuzenleClick = {
                                duzenlenecekUrun = urun
                                yeniFiyatGirdisi = urun.fiyat.toString()
                                yeniStokGirdisi = urun.stok.toString()
                            },
                            onSilClick = { silinecekUrun = urun },
                            onAktiflikDegistir = { yeniDurum -> viewModel.urunAktiflikDegistir(urun.id, yeniDurum) }
                        )
                    }
                }
            }
        }
    }

    // YENİ: Fiyat ve Stok Güncelleme Dialog'u
    if (duzenlenecekUrun != null) {
        AlertDialog(
            onDismissRequest = { duzenlenecekUrun = null },
            title = { Text("Ürünü Düzenle", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
            text = {
                Column {
                    Text("${duzenlenecekUrun?.ad} için bilgileri güncelleyin:", fontSize = 13.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = yeniFiyatGirdisi,
                        onValueChange = { yeniFiyatGirdisi = it },
                        label = { Text("Yeni Fiyat (₺)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = yeniStokGirdisi,
                        onValueChange = { yeniStokGirdisi = it },
                        label = { Text("Mevcut Stok Adedi") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        val yeniFiyat = yeniFiyatGirdisi.toDoubleOrNull()
                        val yeniStok = yeniStokGirdisi.toIntOrNull()
                        if (yeniFiyat != null && yeniStok != null) {
                            viewModel.urunGuncelle(duzenlenecekUrun!!.id, yeniFiyat, yeniStok)
                        }
                        duzenlenecekUrun = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Kaydet", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { duzenlenecekUrun = null }) { Text("İptal", color = Color(0xFF64748B), fontWeight = FontWeight.Bold) }
            }
        )
    }

    // Ürün Silme Onay Dialog'u
    if (silinecekUrun != null) {
        AlertDialog(
            onDismissRequest = { silinecekUrun = null },
            title = { Text("Ürünü Sil", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 18.sp) },
            text = { Text("${silinecekUrun?.ad} adlı ürünü kataloğunuzdan silmek istediğinize emin misiniz? Genellikle ürünleri silmek yerine 'Pasife Almak' tercih edilir.", fontSize = 14.sp, color = Color(0xFF64748B)) },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.urunSil(silinecekUrun!!.id)
                        silinecekUrun = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Evet, Sil", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { silinecekUrun = null }) { Text("İptal", color = Color(0xFF64748B), fontWeight = FontWeight.Bold) }
            }
        )
    }
}

@Composable
fun KatalogYonetimKarti(
    urun: Urun,
    onDuzenleClick: () -> Unit,
    onSilClick: () -> Unit,
    onAktiflikDegistir: (Boolean) -> Unit
) {
    val formatliFiyat = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(urun.fiyat)

    // Eğer ürün pasifse kartın görünürlüğünü (alpha) düşürerek görsel bir efekt veriyoruz
    val kartAlpha = if (urun.aktifMi) 1f else 0.6f

    Card(
        modifier = Modifier.fillMaxWidth().alpha(kartAlpha),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Ürün Görseli
                Box(
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    if (urun.gorselUrl.isNotEmpty()) {
                        AsyncImage(model = urun.gorselUrl, contentDescription = urun.ad, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Default.Image, contentDescription = "Yok", tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
                    }

                    // Pasif Ürün İkonu Overlay
                    if (!urun.aktifMi) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Ürün Detayları
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = urun.ad, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Stok: ${urun.stok} | Min: ${urun.minAlimMiktari}", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$formatliFiyat ₺", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = if (urun.aktifMi) Color(0xFF16A34A) else Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // Alt Kısım: Aktif/Pasif Switch ve Butonlar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

                // Aktiflik Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = urun.aktifMi,
                        onCheckedChange = { onAktiflikDegistir(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF2563EB))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (urun.aktifMi) "Satışta" else "Pasif", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (urun.aktifMi) Color(0xFF2563EB) else Color(0xFF94A3B8))
                }

                // Düzenle ve Sil Butonları
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onDuzenleClick, modifier = Modifier.size(36.dp).background(Color(0xFFF1F5F9), shape = CircleShape)) {
                        Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onSilClick, modifier = Modifier.size(36.dp).background(Color(0xFFFEF2F2), shape = CircleShape)) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}