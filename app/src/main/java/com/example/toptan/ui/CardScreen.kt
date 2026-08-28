package com.example.toptan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.toptan.viewmodel.CartViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: CartViewModel = viewModel()) {
    // ViewModel'den anlık verileri dinliyoruz
    val sepetListesi by viewModel.sepet.collectAsState()
    val toplamTutar by viewModel.toplamTutar.collectAsState()

    // Fiyatı TL formatına çevirme
    val formatliToplam = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(toplamTutar)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Toplu Sepetim", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            CheckoutBar(totalPrice = "$formatliToplam ₺")
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            items(sepetListesi.size) { index ->
                val oge = sepetListesi[index]
                val formatliBirimFiyat = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(oge.urun.fiyat)

                CartItemCard(
                    productName = "${oge.urun.ad} (Min. ${oge.urun.minAlimMiktari})",
                    price = "$formatliBirimFiyat ₺ / Adet",
                    quantity = oge.secilenMiktar,
                    onIncrease = { viewModel.miktarArtir(oge.urun.id) },
                    onDecrease = { viewModel.miktarAzalt(oge.urun.id) }
                )
            }
        }
    }
}

@Composable
fun CartItemCard(
    productName: String,
    price: String,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = price, color = Color(0xFF1565C0), fontWeight = FontWeight.ExtraBold)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease) {
                    Icon(Icons.Default.Remove, contentDescription = "Azalt")
                }
                Text(text = quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onIncrease) {
                    Icon(Icons.Default.Add, contentDescription = "Artır")
                }
            }
        }
    }
}

@Composable
fun CheckoutBar(totalPrice: String) {
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
                onClick = { /* Firebase kayıt işlemi eklenecek */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("Siparişi Tamamla")
            }
        }
    }
}