package com.example.toptan.ui.musteri

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.toptan.viewmodel.musteri.CartViewModel
import com.example.toptan.viewmodel.musteri.ProductDetailViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    urunId: String,
    cartViewModel: CartViewModel,
    detailViewModel: ProductDetailViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val urun by detailViewModel.urun.collectAsState()
    val yukleniyor by detailViewModel.yukleniyor.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val siparisMesaji by cartViewModel.siparisMesaji.collectAsState()

    LaunchedEffect(siparisMesaji) {
        siparisMesaji?.let {
            snackbarHostState.showSnackbar(it)
            cartViewModel.mesajiTemizle()
        }
    }

    LaunchedEffect(urunId) {
        if (urunId.isNotEmpty()) {
            detailViewModel.urunuGetir(urunId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Ürün Detayı", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color(0xFF1E293B))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        bottomBar = {
            if (urun != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                    shadowElevation = 12.dp,
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val formatliFiyat = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(urun!!.fiyat)

                        Column {
                            Text(text = "Birim Fiyat", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "$formatliFiyat ₺", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF1E293B))
                        }

                        Button(
                            onClick = {
                                cartViewModel.sepeteEkle(urun!!)
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Sepete Ekle", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sepete Ekle", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                yukleniyor -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                    }
                }
                urun == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Ürün bulunamadı veya silinmiş olabilir.", color = Color.Gray)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Üst Dev Görsel
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (urun!!.gorselUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = urun!!.gorselUrl,
                                        contentDescription = urun!!.ad,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Image, contentDescription = "Görsel Yok", tint = Color(0xFF94A3B8), modifier = Modifier.size(64.dp))
                                }
                            }
                        }

                        // Ürün Başlığı ve Etiketler
                        item {
                            Column(modifier = Modifier.padding(20.dp)) {
                                // Kategori Rozeti
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDBEAFE), shape = RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = urun!!.kategori.uppercase(Locale.forLanguageTag("tr-TR")),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2563EB)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = urun!!.ad,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E293B),
                                    lineHeight = 30.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // B2B Bilgi Paneli (Stok ve Min Alım)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Inventory2, contentDescription = "Stok", tint = Color(0xFF64748B), modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Stok Durumu", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                            Text("${urun!!.stok} Adet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        }

                                        Divider(modifier = Modifier.height(40.dp).width(1.dp), color = Color(0xFFE2E8F0))

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.ShoppingCart, contentDescription = "Min Alım", tint = Color(0xFF64748B), modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Min. Alım", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                            Text("${urun!!.minAlimMiktari} Adet", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}