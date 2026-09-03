package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.toptan.viewmodel.HomeViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToCatalog: (String) -> Unit // 1. DEĞİŞİKLİK: Artık ID (String) beklediğini söyledik
) {
    // ViewModel'deki listeyi dinliyoruz
    val toptanciListesi by viewModel.toptancilar.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // 1. Arama Çubuğu
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Toptancı veya toplu ürün ara...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Kategori Çipleri
        Text("Kategoriler", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        val categories = listOf("Tümü", "Gıda", "Tekstil", "Elektronik", "Ambalaj", "Temizlik")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories.size) { index ->
                FilterChip(
                    selected = index == 0,
                    onClick = { },
                    label = { Text(categories[index]) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF1565C0),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Toptancı Listesi
        Text("Öne Çıkan Toptancılar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(toptanciListesi.size) { index ->
                val toptanci = toptanciListesi[index]

                val formatliFiyat = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(toptanci.minSiparisTutari)

                WholesalerCard(
                    name = toptanci.ad,
                    minOrder = "$formatliFiyat ₺",
                    ayniGunKargo = toptanci.ayniGunKargo,
                    onayliMi = toptanci.onayliMi,
                    // 2. DEĞİŞİKLİK: Toptancıya tıklandığında onun ID'sini yolluyoruz
                    onCatalogClick = { onNavigateToCatalog(toptanci.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun WholesalerCard(name: String, minOrder: String, ayniGunKargo: Boolean, onayliMi: Boolean, onCatalogClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = "Onaylı",
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Min. Sepet Tutarı: $minOrder", fontSize = 14.sp, color = Color.DarkGray)
            Text(text = "Aynı Gün Kargo", fontSize = 14.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick =  onCatalogClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text("Kataloğu İncele")
            }
        }
    }
}