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
import com.example.toptan.viewmodel.musteri.CartViewModel
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
    val toptanciMinLimit by viewModel.toptanciMinLimit.collectAsState()
    val iskontoOrani by viewModel.iskontoOrani.collectAsState()

    LaunchedEffect(siparisMesaji) {
        if (siparisMesaji != null) {
            delay(3000)
            viewModel.mesajiTemizle()
        }
    }

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
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (sepetListesi.isNotEmpty()) {
                                Text(
                                    text = "${sepetListesi.size} farklı ürün",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    actions = {
                        if (sepetListesi.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.sepetiTemizle() },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(38.dp)
                                    .background(Color(0xFFFEF2F2).copy(alpha = 0.1f), shape = CircleShape)
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
                        AnimatedVisibility(
                            visible = !siparisMesaji.isNullOrEmpty() && !siparisMesaji.orEmpty().contains("Oluşturuldu"),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFFEF2F2).copy(alpha = 0.9f)
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
                            totalPrice = toplamTutar,
                            minLimit = toptanciMinLimit,
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
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), Color.Transparent)
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
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Boş Sepet",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text("Sepetiniz şu an boş", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Katalogdan toptan ürün seçerek sepetinizi hemen doldurabilirsiniz.",
                                fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), textAlign = TextAlign.Center,
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
                            val indirimliFiyat = oge.urun.fiyat * (1 - (iskontoOrani / 100.0))
                            val formatliBirimFiyat = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR")).format(indirimliFiyat)

                            val fiyatMetni = if (iskontoOrani > 0.0) {
                                "$formatliBirimFiyat ₺ (VIP İndirimli)"
                            } else {
                                "$formatliBirimFiyat ₺"
                            }

                            CartItemCard(
                                productName = oge.urun.ad,
                                minOrderText = "Min. Alım: ${oge.urun.minAlimMiktari} Adet",
                                price = fiyatMetni,
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

@Composable
fun OrderSuccessScreen(onContinueShopping: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color(0xFF10B981).copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Başarılı",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(70.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Siparişiniz Başarıyla Alındı!",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Siparişiniz toptancıya güvenle iletildi. Durumu 'Siparişler' sekmesinden anlık olarak takip edebilirsiniz.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Alışverişe Devam Et", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
            }
        }
    }
}

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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
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
                    Icon(Icons.Default.Image, contentDescription = "Görsel Yok", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(26.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = minOrderText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = price,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Azalt", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            text = quantity.toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Artır", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFFEF2F2).copy(alpha = 0.1f), shape = CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutBar(totalPrice: Double, minLimit: Double, onCheckoutClick: () -> Unit) {
    val formatliToplam = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(totalPrice)
    val progress = if (minLimit > 0) (totalPrice / minLimit).coerceIn(0.0, 1.0).toFloat() else 1f
    val kalan = if (minLimit > totalPrice) minLimit - totalPrice else 0.0
    val formatliKalan = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(kalan)
    val isReady = progress >= 1f

    Surface(
        modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (minLimit > 0) {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = if (isReady) "🎉 Harika! Minimum limiti aştınız." else "Siparişi tamamlamak için $formatliKalan ₺ eksik",
                                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isReady) Color(0xFF10B981) else Color(0xFFD97706)
                            )
                            Text(text = "%${(progress * 100).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isReady) Color(0xFF10B981) else Color(0xFFD97706))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                            color = if (isReady) Color(0xFF10B981) else Color(0xFFD97706),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Genel Toplam", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$formatliToplam ₺", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Button(
                    onClick = onCheckoutClick,
                    enabled = isReady,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(if (isReady) "Siparişi Tamamla" else "Limit Yetersiz", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}