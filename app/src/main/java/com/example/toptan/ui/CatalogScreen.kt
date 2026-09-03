package com.example.toptan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.model.Urun
import com.example.toptan.viewmodel.CartViewModel
import com.example.toptan.viewmodel.CatalogViewModel
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Image
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    toptanciId: String, // 1. MainScreen'den gönderilen ID'yi buradan alıyoruz
    cartViewModel: CartViewModel,
    catalogViewModel: CatalogViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    // 2. Ekran açıldığı an bu ID'yi ViewModel'e yollayıp ürünleri çektiriyoruz
    LaunchedEffect(toptanciId) {
        catalogViewModel.urunleriGetir(toptanciId)
    }

    val urunler by catalogViewModel.urunler.collectAsState()
    val yukleniyor by catalogViewModel.yukleniyor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marmara Toptan Gıda", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (yukleniyor) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (urunler.isEmpty()) {
                Text(
                    text = "Bu toptancıya ait henüz ürün bulunmuyor.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Toptan Ürün Kataloğu",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(urunler.size) { index ->
                        CatalogItemCard(
                            urun = urunler[index],
                            onAddToCartClick = { cartViewModel.sepeteEkle(urunler[index]) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun CatalogItemCard(urun: Urun, onAddToCartClick: () -> Unit) {
    val formatliFiyat = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(urun.fiyat)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- 1. GÖRSEL ALANI (SOL TARAF) ---
            if (urun.gorselUrl.isNotEmpty()) {
                AsyncImage(
                    model = urun.gorselUrl,
                    contentDescription = urun.ad,
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp)), // Köşeleri hafif yuvarlatılmış kare
                    contentScale = ContentScale.Crop
                )
            } else {
                // Eğer ürünün fotoğrafı yoksa şık bir gri yer tutucu (placeholder) gösteriyoruz
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Görsel Yok", tint = Color.Gray, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // --- 2. ÜRÜN BİLGİLERİ VE BUTON (SAĞ TARAF) ---
            Column(modifier = Modifier.weight(1f)) {
                Text(text = urun.ad, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))

                Text(text = "$formatliFiyat ₺ / Adet", color = Color(0xFF1565C0), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text(text = "Min. Alım: ${urun.minAlimMiktari} Adet", fontSize = 12.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                // Butonu sağa yaslıyoruz
                Button(
                    onClick = onAddToCartClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Ekle", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sepete Ekle")
                }
            }
        }
    }
}