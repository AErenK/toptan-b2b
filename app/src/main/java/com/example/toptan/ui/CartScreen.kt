package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.toptan.viewmodel.CartViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: CartViewModel = viewModel()) {
    val sepetListesi by viewModel.sepet.collectAsState()
    val toplamTutar by viewModel.toplamTutar.collectAsState()
    val siparisMesaji by viewModel.siparisMesaji.collectAsState()

    LaunchedEffect(siparisMesaji) {
        if (siparisMesaji != null) {
            delay(3000)
            viewModel.mesajiTemizle()
        }
    }

    val formatliToplam = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(toplamTutar)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toplu Sepetim", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    if (sepetListesi.isNotEmpty()) {
                        IconButton(onClick = { viewModel.sepetiTemizle() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Sepeti Temizle", tint = Color.Red)
                        }
                    }
                }
            )
        },
        bottomBar = {
            // YENİ: Sepet boşsa alt kısımdaki ödeme barını tamamen gizliyoruz
            if (sepetListesi.isNotEmpty()) {
                Column {
                    siparisMesaji?.let { mesaj ->
                        Text(
                            text = mesaj,
                            color = if (mesaj.contains("Başarı")) Color(0xFF2E7D32) else Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    CheckoutBar(
                        totalPrice = "$formatliToplam ₺",
                        onCheckoutClick = {
                            val siparisOzeti = sepetListesi.joinToString(", ") {
                                "${it.secilenMiktar}x ${it.urun.ad}"
                            }
                            viewModel.siparisiTamamla(
                                toplamTutar = toplamTutar,
                                sepetOzet = siparisOzeti
                            )
                        }
                    )
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->

        // YENİ: Boş Ekran (Empty State) Kontrolü
        if (sepetListesi.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Boş Sepet",
                        tint = Color.LightGray,
                        modifier = Modifier.size(120.dp) // Büyük ve şık bir ikon
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sepetiniz şu an boş",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Katalogdan ürün seçerek sepetinizi hemen doldurabilirsiniz.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            // Sepet doluysa listeyi göster
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(
                    items = sepetListesi,
                    key = { oge -> oge.urun.id }
                ) { oge ->
                    val formatliBirimFiyat = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(oge.urun.fiyat)

                    CartItemCard(
                        productName = "${oge.urun.ad} (Min. ${oge.urun.minAlimMiktari})",
                        price = "$formatliBirimFiyat ₺",
                        quantity = oge.secilenMiktar,
                        imageUrl = oge.urun.gorselUrl,
                        onIncrease = { viewModel.miktarArtir(oge.urun.id) },
                        onDecrease = { viewModel.miktarAzalt(oge.urun.id) },
                        onDelete = { viewModel.urunuSil(oge.urun.id) }
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun CartItemCard(
    productName: String,
    price: String,
    quantity: Int,
    imageUrl: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = productName,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F0F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Görsel Yok", tint = Color.Gray, modifier = Modifier.size(24.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = productName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = price, color = Color(0xFF1565C0), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Azalt", modifier = Modifier.size(20.dp))
                    }

                    Text(
                        text = quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Artır", modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFFFEBEE), shape = CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Ürünü Sil", tint = Color.Red, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutBar(
    totalPrice: String,
    onCheckoutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 16.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Genel Toplam", fontSize = 12.sp, color = Color.Gray)
                Text(text = totalPrice, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Button(
                onClick = onCheckoutClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Siparişi Tamamla")
            }
        }
    }
}