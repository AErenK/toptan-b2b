package com.example.toptan.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.toptan.viewmodel.AuthViewModel
import com.example.toptan.viewmodel.ToptanciViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToptanciHomeScreen(
    viewModel: ToptanciViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onLogoutClick: () -> Unit,
    onNavigateToSiparisler: () -> Unit
) {
    var urunAdi by remember { mutableStateOf("") }
    var fiyat by remember { mutableStateOf("") }
    var minAlim by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }
    var gorselUri by remember { mutableStateOf<Uri?>(null) }

    val mesaj by viewModel.mesaj.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        gorselUri = uri
    }

    LaunchedEffect(mesaj) {
        if (mesaj?.contains("başarıyla") == true) {
            delay(3000)
            urunAdi = ""
            fiyat = ""
            minAlim = ""
            stok = ""
            gorselUri = null
            viewModel.mesajiTemizle()
        } else if (mesaj != null) {
            delay(3000)
            viewModel.mesajiTemizle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toptancı Paneli", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC)),
                actions = {
                    IconButton(onClick = {
                        authViewModel.cikisYap()
                        onLogoutClick()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Çıkış", tint = Color(0xFFEF4444))
                    }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onNavigateToSiparisler,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.List, contentDescription = "Siparişler", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gelen Siparişleri Görüntüle", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Yeni Ürün Ekle",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE2E8F0))
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (gorselUri != null) {
                    AsyncImage(
                        model = gorselUri,
                        contentDescription = "Seçilen Görsel",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = "Görsel Seç", tint = Color(0xFF64748B), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fotoğraf Seçmek İçin Dokunun", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = urunAdi,
                onValueChange = { urunAdi = it },
                label = { Text("Ürün Adı (Örn: 5L Ayçiçek Yağı)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = fiyat,
                onValueChange = { fiyat = it },
                label = { Text("Birim Fiyat (₺)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2563EB),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
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
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = stok,
                    onValueChange = { stok = it },
                    label = { Text("Mevcut Stok") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.urunEkle(urunAdi, fiyat, minAlim, stok, gorselUri) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Ekle", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kataloğa Ekle", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            mesaj?.let { m ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = m,
                    color = if (m.contains("başarıyla")) Color(0xFF16A34A) else Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}