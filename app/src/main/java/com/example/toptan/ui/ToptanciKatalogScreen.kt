package com.example.toptan.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.toptan.model.Urun
import com.example.toptan.viewmodel.ToptanciViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToptanciKatalogScreen(
    viewModel: ToptanciViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val urunler by viewModel.toptanciUrunleri.collectAsState()
    val mesaj by viewModel.mesaj.collectAsState()
    val context = LocalContext.current

    // Dialog Kontrolleri
    var silinecekUrun by remember { mutableStateOf<Urun?>(null) }
    var fiyatGuncellenecekUrun by remember { mutableStateOf<Urun?>(null) }
    var yeniFiyatGirdisi by remember { mutableStateOf("") }

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
                title = { Text("Kataloğum", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, "Geri") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        if (urunler.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Inventory, contentDescription = "Boş", tint = Color.LightGray, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Kataloğunuzda henüz ürün yok", fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(urunler.size) { index ->
                    val urun = urunler[index]
                    KatalogYonetimKarti(
                        urun = urun,
                        onFiyatGuncelleClick = {
                            fiyatGuncellenecekUrun = urun
                            yeniFiyatGirdisi = urun.fiyat.toString()
                        },
                        onSilClick = { silinecekUrun = urun }
                    )
                }
            }
        }
    }

    // Fiyat Güncelleme Dialog'u
    if (fiyatGuncellenecekUrun != null) {
        AlertDialog(
            onDismissRequest = { fiyatGuncellenecekUrun = null },
            title = { Text("Fiyat Güncelle", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("${fiyatGuncellenecekUrun?.ad} için yeni fiyatı girin:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = yeniFiyatGirdisi,
                        onValueChange = { yeniFiyatGirdisi = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val yeniFiyat = yeniFiyatGirdisi.toDoubleOrNull()
                    if (yeniFiyat != null) {
                        viewModel.fiyatGuncelle(fiyatGuncellenecekUrun!!.id, yeniFiyat)
                    }
                    fiyatGuncellenecekUrun = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { fiyatGuncellenecekUrun = null }) { Text("İptal", color = Color.Gray) }
            }
        )
    }

    // Ürün Silme Onay Dialog'u
    if (silinecekUrun != null) {
        AlertDialog(
            onDismissRequest = { silinecekUrun = null },
            title = { Text("Ürünü Sil", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444)) },
            text = { Text("${silinecekUrun?.ad} adlı ürünü kataloğunuzdan silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.urunSil(silinecekUrun!!.id)
                    silinecekUrun = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) {
                    Text("Evet, Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { silinecekUrun = null }) { Text("İptal", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun KatalogYonetimKarti(urun: Urun, onFiyatGuncelleClick: () -> Unit, onSilClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Ürün Görseli
            if (urun.gorselUrl.isNotEmpty()) {
                AsyncImage(
                    model = urun.gorselUrl, contentDescription = urun.ad, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Image, contentDescription = "Yok", tint = Color(0xFF94A3B8)) }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Ürün Bilgileri
            Column(modifier = Modifier.weight(1f)) {
                Text(urun.ad, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Stok: ${urun.stok} | Min: ${urun.minAlimMiktari}", fontSize = 12.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(4.dp))
                Text("${urun.fiyat} ₺", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF16A34A))
            }

            // Aksiyon Butonları
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onFiyatGuncelleClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color(0xFF2563EB))
                }
                IconButton(onClick = onSilClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}