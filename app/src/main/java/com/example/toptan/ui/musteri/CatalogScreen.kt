package com.example.toptan.ui.musteri

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.toptan.model.Urun
import com.example.toptan.viewmodel.CartViewModel
import com.example.toptan.viewmodel.CatalogViewModel
import java.text.NumberFormat
import java.util.Locale

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

    var aramaMetni by remember { mutableStateOf("") }

    val filtrelenmisUrunler = remember(urunler, aramaMetni) {
        if (aramaMetni.isBlank()) urunler
        else urunler.filter { it.ad.contains(aramaMetni, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Toptancı Kataloğu",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "${urunler.size} ürün listeleniyor",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
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

            // Hafif Üst Gradyan Efekti
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF2563EB).copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {

                // ŞIK VE AKICI ARAMA ÇUBUĞU
                OutlinedTextField(
                    value = aramaMetni,
                    onValueChange = { aramaMetni = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(54.dp),
                    placeholder = { Text("Kataloğda ürün ara...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara", tint = Color(0xFF64748B)) },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = aramaMetni.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(onClick = { aramaMetni = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Temizle", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                when {
                    yukleniyor -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF2563EB))
                        }
                    }
                    urunler.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color(0xFFE2E8F0), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(36.dp))
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Bu toptancıya ait ürün bulunmuyor.", color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                    filtrelenmisUrunler.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Aradığınız kriterlere uygun ürün bulunamadı.", color = Color(0xFF64748B), fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                        ) {
                            items(filtrelenmisUrunler.size) { index ->
                                CatalogItemCard(
                                    urun = filtrelenmisUrunler[index],
                                    onAddToCartClick = { cartViewModel.sepeteEkle(filtrelenmisUrunler[index]) }
                                )
                            }
                        }
                    }
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Şık Görsel Alanı
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (urun.gorselUrl.isNotEmpty()) {
                    AsyncImage(
                        model = urun.gorselUrl,
                        contentDescription = urun.ad,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Görsel Yok",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Ürün Detayları ve Aksiyon Butonu
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = urun.ad,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$formatliFiyat ₺ / Adet",
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Min. Alım: ${urun.minAlimMiktari} Adet",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sepete Ekle Butonu
                Button(
                    onClick = onAddToCartClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Ekle",
                        modifier = Modifier.size(15.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Sepete Ekle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}