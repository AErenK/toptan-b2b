package com.example.toptan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.viewmodel.AuthViewModel
import com.example.toptan.viewmodel.ToptanciViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToptanciHomeScreen(
    viewModel: ToptanciViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onLogoutClick: () -> Unit,
    onNavigateToSiparisler: () -> Unit // YENİ: Siparişler ekranına gitme tetikleyicisi
) {
    var urunAdi by remember { mutableStateOf("") }
    var fiyat by remember { mutableStateOf("") }
    var minAlim by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }

    val mesaj by viewModel.mesaj.collectAsState()

    LaunchedEffect(mesaj) {
        if (mesaj?.contains("başarıyla") == true) {
            delay(3000)
            urunAdi = ""
            fiyat = ""
            minAlim = ""
            stok = ""
            viewModel.mesajiTemizle()
        } else if (mesaj != null) {
            delay(3000)
            viewModel.mesajiTemizle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toptancı Paneli", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0)),
                actions = {
                    IconButton(onClick = {
                        authViewModel.cikisYap()
                        onLogoutClick()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // YENİ EKLENEN BUTON: Gelen Siparişlere Git
            Button(
                onClick = onNavigateToSiparisler,
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)), // Turuncu renk
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.List, contentDescription = "Siparişler", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gelen Siparişleri Görüntüle", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Divider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Yeni Ürün Ekle",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = "Müşterilerin kataloğunda görünecek ürünü tanımlayın.",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = urunAdi,
                onValueChange = { urunAdi = it },
                label = { Text("Ürün Adı (Örn: 5L Ayçiçek Yağı)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fiyat,
                onValueChange = { fiyat = it },
                label = { Text("Birim Fiyatı (₺)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = minAlim,
                    onValueChange = { minAlim = it },
                    label = { Text("Min. Alım") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = stok,
                    onValueChange = { stok = it },
                    label = { Text("Mevcut Stok") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.urunEkle(urunAdi, fiyat, minAlim, stok) },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Ekle", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kataloğa Ürün Ekle", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            mesaj?.let { m ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = m,
                    color = if (m.contains("başarıyla")) Color(0xFF2E7D32) else Color.Red,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}