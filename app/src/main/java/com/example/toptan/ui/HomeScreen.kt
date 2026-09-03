package com.example.toptan.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.rounded.ArrowForwardIos
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
    onNavigateToCatalog: (String) -> Unit
) {
    val toptanciListesi by viewModel.toptancilar.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Modern açık arkaplan
            .padding(top = 16.dp)
    ) {
        // Üst Kısım: Padding içinde
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Toptancıları Keşfet",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Modern Arama Çubuğu
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Toptancı veya ürün ara...", color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara", tint = Color(0xFF64748B)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50), // Tam yuvarlak pill shape
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedBorderColor = Color(0xFF2563EB)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("Kategoriler", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Kategori Çipleri
        val categories = listOf("Tümü", "Gıda", "Tekstil", "Elektronik", "Ambalaj", "Temizlik")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(categories.size) { index ->
                FilterChip(
                    selected = index == 0,
                    onClick = { },
                    label = { Text(categories[index], fontWeight = FontWeight.Medium) },
                    shape = RoundedCornerShape(50),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF2563EB),
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color(0xFF64748B)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = index == 0,
                        borderColor = Color(0xFFE2E8F0)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toptancı Listesi
        Text(
            text = "Öne Çıkan Toptancılar",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1E293B),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(toptanciListesi.size) { index ->
                val toptanci = toptanciListesi[index]
                val formatliFiyat = NumberFormat.getNumberInstance(Locale("tr", "TR")).format(toptanci.minSiparisTutari)

                WholesalerCard(
                    name = toptanci.ad,
                    minOrder = "$formatliFiyat ₺",
                    ayniGunKargo = toptanci.ayniGunKargo,
                    onayliMi = toptanci.onayliMi,
                    onCatalogClick = { onNavigateToCatalog(toptanci.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) } // Alt menü boşluğu
        }
    }
}

@Composable
fun WholesalerCard(name: String, minOrder: String, ayniGunKargo: Boolean, onayliMi: Boolean, onCatalogClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp), // Daha modern yuvarlak köşe
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onCatalogClick // Artık karta tıklayınca direkt gidiyor!
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E293B))
                    if (onayliMi) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Onaylı",
                            tint = Color(0xFF3B82F6), // Güven veren mavi
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Text(text = "Min. Sepet: $minOrder", fontSize = 14.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)

                if (ayniGunKargo) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Hızlı Teslimat", fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Sağdaki modern ok işareti
            Icon(
                imageVector = Icons.Rounded.ArrowForwardIos,
                contentDescription = "İncele",
                tint = Color(0xFFCBD5E1),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}