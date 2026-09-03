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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
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
                title = { Text("Sepetim", fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC)),
                actions = {
                    if (sepetListesi.isNotEmpty()) {
                        IconButton(onClick = { viewModel.sepetiTemizle() }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Temizle", tint = Color(0xFFEF4444))
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column {
                siparisMesaji?.let { mesaj ->
                    Surface(
                        color = if (mesaj.contains("Başarı")) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = mesaj,
                            color = if (mesaj.contains("Başarı")) Color(0xFF16A34A) else Color(0xFFEF4444),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                CheckoutBar(
                    totalPrice = "$formatliToplam ₺",
                    onCheckoutClick = {
                        if (sepetListesi.isNotEmpty()) {
                            val siparisOzeti = sepetListesi.joinToString(", ") {
                                "${it.secilenMiktar}x ${it.urun.ad}"
                            }
                            viewModel.siparisiTamamla(
                                toplamTutar = toplamTutar,
                                sepetOzet = siparisOzeti
                            )
                        }
                    }
                )
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(items = sepetListesi, key = { oge -> oge.urun.id }) { oge ->
                val formatliBirimFiyat = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(oge.urun.fiyat)

                CartItemCard(
                    productName = oge.urun.ad,
                    minAlim = "Min. ${oge.urun.minAlimMiktari} Adet",
                    price = "$formatliBirimFiyat ₺",
                    quantity = oge.secilenMiktar,
                    onIncrease = { viewModel.miktarArtir(oge.urun.id) },
                    onDecrease = { viewModel.miktarAzalt(oge.urun.id) },
                    onDelete = { viewModel.urunuSil(oge.urun.id) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun CartItemCard(
    productName: String,
    minAlim: String,
    price: String,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat, temiz görünüm
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = productName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = minAlim, fontSize = 12.sp, color = Color(0xFF94A3B8))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = price, color = Color(0xFF2563EB), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Miktar Kontrol Kapsülü
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFF1F5F9), shape = RoundedCornerShape(50))
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Azalt", tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
                }
                Text(
                    text = quantity.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Artır", tint = Color(0xFF1E293B), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Silme Butonu
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFEE2E2), shape = CircleShape)
            ) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Sil", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun CheckoutBar(totalPrice: String, onCheckoutClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 24.dp, // Derinlik hissiyatı için
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .navigationBarsPadding() // Alt barlar (ekran altı) için boşluk
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Toplam Tutar", fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Text(text = totalPrice, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF1E293B))
            }
            Button(
                onClick = onCheckoutClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Başarı/Güven yeşili
                modifier = Modifier.height(56.dp).padding(start = 16.dp)
            ) {
                Text("Siparişi Tamamla", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}