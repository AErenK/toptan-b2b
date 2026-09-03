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
    toptanciId: String,
    cartViewModel: CartViewModel,
    catalogViewModel: CatalogViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    LaunchedEffect(toptanciId) {
        catalogViewModel.urunleriGetir(toptanciId)
    }

    val urunler by catalogViewModel.urunler.collectAsState()
    val yukleniyor by catalogViewModel.yukleniyor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toptancı Kataloğu", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (yukleniyor) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF2563EB))
            } else if (urunler.isEmpty()) {
                Text(
                    text = "Bu toptancıya ait ürün bulunmuyor.",
                    color = Color(0xFF64748B),
                    modifier = Modifier.align(Alignment.Center),
                    fontWeight = FontWeight.Medium
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (urun.gorselUrl.isNotEmpty()) {
                AsyncImage(
                    model = urun.gorselUrl,
                    contentDescription = urun.ad,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Görsel Yok", tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = urun.ad, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$formatliFiyat ₺ / Adet", color = Color(0xFF2563EB), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Min. Alım: ${urun.minAlimMiktari} Adet", fontSize = 12.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onAddToCartClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Ekle", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sepete Ekle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}