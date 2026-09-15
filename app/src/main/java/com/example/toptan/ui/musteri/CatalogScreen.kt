package com.example.toptan.ui.musteri

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.toptan.model.Urun
import com.example.toptan.viewmodel.CatalogViewModel
import com.example.toptan.viewmodel.musteri.CartViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    toptanciId: String,
    cartViewModel: CartViewModel,
    catalogViewModel: CatalogViewModel = viewModel(),
    onBackClick: () -> Unit,
    onUrunClick: (String) -> Unit
) {
    val urunler by catalogViewModel.urunler.collectAsState()
    val yukleniyor by catalogViewModel.yukleniyor.collectAsState()
    val aramaMetni by catalogViewModel.aramaMetni.collectAsState()
    val seciliKategori by catalogViewModel.seciliKategori.collectAsState()
    val kategoriler by catalogViewModel.kategoriler.collectAsState()
    val favoriUrunIds by catalogViewModel.favoriUrunIds.collectAsState()
    val siparisMesaji by cartViewModel.siparisMesaji.collectAsState()
    val iskontoOrani by cartViewModel.iskontoOrani.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(toptanciId) {
        catalogViewModel.urunleriGetir(toptanciId)
    }

    LaunchedEffect(siparisMesaji) {
        siparisMesaji?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            cartViewModel.mesajiTemizle()
        }
    }

    val filtrelenmisUrunler = urunler.filter { urun ->
        val aramaUyumu = urun.ad.contains(aramaMetni, ignoreCase = true)
        val kategoriUyumu = when (seciliKategori) {
            "Tümü" -> true
            "Favorilerim" -> favoriUrunIds.contains(urun.id)
            else -> urun.kategori == seciliKategori
        }
        aramaUyumu && kategoriUyumu
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ürün Kataloğu", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = MaterialTheme.colorScheme.onSurface) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
                OutlinedTextField(
                    value = aramaMetni, onValueChange = { catalogViewModel.aramaMetniGuncelle(it) },
                    placeholder = { Text("Katalogda Ürün Ara...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            if (kategoriler.size > 1) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(kategoriler) { kategori ->
                        val isSelected = seciliKategori == kategori
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                                .clickable { catalogViewModel.kategoriSec(kategori) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (kategori == "Favorilerim") {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.background else Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = kategori,
                                    color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            if (yukleniyor) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }
            } else if (filtrelenmisUrunler.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (seciliKategori == "Favorilerim") "Henüz favoriye eklediğiniz ürün yok." else "Aradığınız kriterlere uygun ürün bulunamadı.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(filtrelenmisUrunler) { urun ->
                        val isFavorite = favoriUrunIds.contains(urun.id)
                        MusteriUrunKarti(
                            urun = urun,
                            iskontoOrani = iskontoOrani,
                            isFavorite = isFavorite,
                            onFavoriteClick = { catalogViewModel.favoriDurumuDegistir(urun.id) },
                            onClick = { onUrunClick(urun.id) },
                            onSepeteEkle = { cartViewModel.sepeteEkle(urun) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MusteriUrunKarti(
    urun: Urun,
    iskontoOrani: Double,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    onSepeteEkle: () -> Unit
) {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("tr-TR"))
    val indirimliFiyat = urun.fiyat * (1 - (iskontoOrani / 100.0))
    val normalFiyatStr = formatter.format(urun.fiyat)
    val indirimliFiyatStr = formatter.format(indirimliFiyat)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (urun.gorselUrl.isNotEmpty()) {
                    AsyncImage(model = urun.gorselUrl, contentDescription = urun.ad, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Image, contentDescription = "Yok", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(24.dp))
                }

                if (iskontoOrani > 0) {
                    Box(modifier = Modifier.align(Alignment.BottomStart).background(Color(0xFFEF4444), RoundedCornerShape(topEnd = 8.dp, bottomStart = 12.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("%${iskontoOrani.toInt()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape).clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = urun.ad, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Kategori: ${urun.kategori.ifEmpty { "Belirtilmemiş" }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Min. Alım: ${urun.minAlimMiktari} Adet", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(6.dp))

                if (iskontoOrani > 0) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "$normalFiyatStr ₺", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), textDecoration = TextDecoration.LineThrough)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "$indirimliFiyatStr ₺", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF10B981))
                    }
                } else {
                    Text(text = "$normalFiyatStr ₺", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSepeteEkle,
                modifier = Modifier.size(42.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
            ) { Icon(Icons.Default.AddShoppingCart, contentDescription = "Sepete Ekle", tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(20.dp)) }
        }
    }
}