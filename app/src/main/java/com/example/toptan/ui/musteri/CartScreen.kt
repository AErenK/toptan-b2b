package com.example.toptan.ui.musteri

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    val siparisBasarili by viewModel.siparisBasarili.collectAsState()

    LaunchedEffect(siparisMesaji) {
        if (siparisMesaji != null) {
            delay(3000)
            viewModel.mesajiTemizle()
        }
    }

    val formatliToplam = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(toplamTutar)

    if (siparisBasarili) {
        OrderSuccessScreen(onContinueShopping = {
            viewModel.siparisBasariliDurumunuSifirla()
        })
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Toplu Sepetim",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E293B)
                            )
                            if (sepetListesi.isNotEmpty()) {
                                Text(
                                    text = "${sepetListesi.size} farklı ürün",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC)),
                    actions = {
                        if (sepetListesi.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.sepetiTemizle() },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(38.dp)
                                    .background(Color(0xFFFEF2F2), shape = CircleShape)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Sepeti Temizle", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (sepetListesi.isNotEmpty()) {
                    Column {
                        // Hata veya bilgi mesajları için şık bildirim çubuğu
                        AnimatedVisibility(
                            visible = !siparisMesaji.isNullOrEmpty() && !siparisMesaji.orEmpty().contains("Oluşturuldu"),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFEF2F2)
                            ) {
                                Text(
                                    text = siparisMesaji ?: "",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
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
            containerColor = Color(0xFFF8FAFC)
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                // Arka plana hafif tasarım derinliği
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF2563EB).copy(alpha = 0.04f), Color.Transparent)
                            )
                        )
                )

                if (sepetListesi.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(Color(0xFFE2E8F0), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Boş Sepet",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text("Sepetiniz şu an boş", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Katalogdan toptan ürün seçerek sepetinizi hemen doldurabilirsiniz.",
                                fontSize = 13.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
                    ) {
                        items(
                            items = sepetListesi,
                            key = { oge -> oge.urun.id }
                        ) { oge ->
                            val formatliBirimFiyat = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(oge.urun.fiyat)

                            CartItemCard(
                                productName = oge.urun.ad,
                                minOrderText = "Min. Alım: ${oge.urun.minAlimMiktari} Adet",
                                price = "$formatliBirimFiyat ₺",
                                quantity = oge.secilenMiktar,
                                imageUrl = oge.urun.gorselUrl,
                                onIncrease = { viewModel.miktarArtir(oge.urun.id) },
                                onDecrease = { viewModel.miktarAzalt(oge.urun.id) },
                                onDelete = { viewModel.urunuSil(oge.urun.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// TAM EKRAN BAŞARILI SİPARİŞ EKRANI (Daha Premium & Akıcı)
@Composable
fun OrderSuccessScreen(onContinueShopping: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color(0xFFDCFCE7), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Başarılı",
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(70.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Siparişiniz Başarıyla Alındı!",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Siparişiniz toptancıya güvenle iletildi. Durumu 'Siparişler' sekmesinden anlık olarak takip edebilirsiniz.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = onContinueShopping,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Alışverişe Devam Et", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// SEPET ÜRÜN KARTI (Modern çizgi, oranlar ve arayüz yapısı)
@Composable
fun CartItemCard(
    productName: String,
    minOrderText: String,
    price: String,
    quantity: Int,
    imageUrl: String,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Görsel Kutusu
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = productName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, contentDescription = "Görsel Yok", tint = Color(0xFF94A3B8), modifier = Modifier.size(26.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = minOrderText,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = price,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Miktar Arttırma/Azaltma ve Silme Paneli
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Miktar Kontrol Kutusu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Azalt", modifier = Modifier.size(16.dp), tint = Color(0xFF475569))
                        }
                        Text(
                            text = quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Artır", modifier = Modifier.size(16.dp), tint = Color(0xFF475569))
                        }
                    }

                    // Sil Butonu
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFFEF2F2), shape = CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Ürünü Sil", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// SEPET ALT ÖDEME ÇUBUĞU (Checkout Bar)
@Composable
fun CheckoutBar(totalPrice: String, onCheckoutClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
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
            Column {
                Text(text = "Genel Toplam", fontSize = 12.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = totalPrice, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1E293B))
            }
            Button(
                onClick = onCheckoutClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Siparişi Tamamla", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}