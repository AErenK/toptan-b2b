package com.example.toptan.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.toptan.model.Urun
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(onBackClick: () -> Unit) {
    // Şimdilik sahte (dummy) ürün listesi oluşturuyoruz
    val urunler = listOf(
        Urun(id = "U1", toptanciId = "1", ad = "Torku Küp Şeker 1KG (Koli)", fiyat = 250.0, minAlimMiktari = 50, stokMiktari = 500),
        Urun(id = "U2", toptanciId = "1", ad = "Doğuş Çay 1KG (Koli)", fiyat = 600.0, minAlimMiktari = 20, stokMiktari = 200),
        Urun(id = "U3", toptanciId = "1", ad = "Yudum Ayçiçek Yağı 5L (Koli)", fiyat = 850.0, minAlimMiktari = 10, stokMiktari = 100)
    )

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                CatalogItemCard(urun = urunler[index])
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun CatalogItemCard(urun: Urun) {
    val formatliFiyat = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(urun.fiyat)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = urun.ad, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "$formatliFiyat ₺ / Adet", color = Color(0xFF1565C0), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(text = "Min. Alım: ${urun.minAlimMiktari} Adet", fontSize = 12.sp, color = Color.Gray)
                }

                Button(
                    onClick = { /* Sepete ekleme işlemi MVVM ile bağlanacak */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Ekle", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekle")
                }
            }
        }
    }
}