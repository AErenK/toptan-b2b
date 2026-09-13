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
    val favoriUrunIds by catalogViewModel.favoriUrunIds.collectAsState() // YENİ
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

    // YENİ: "Favorilerim" seçiliyse sadece favori listesindeki ID'lere sahip olanları göster
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
                title = { Text("Ürün Kataloğu", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color(0xFF1E293B)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.background(Color.White).padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
                OutlinedTextField(
                    value = aramaMetni, onValueChange = { catalogViewModel.aramaMetniGuncelle(it) },
                    placeholder = { Text("Katalogda Ürün Ara...", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB), unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF1F5F9), unfocusedContainerColor = Color(0xFFF1F5F9)
                    )
                )
            }

            if (kategoriler.size > 1) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(kategoriler) { kategori ->
                        val isSelected = seciliKategori == kategori
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF2563EB) else Color(0xFFF1F5F9))
                                .clickable { catalogViewModel.kategoriSec(kategori) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // YENİ: "Favorilerim" sekmesine özel kırmızımsı kalp ikonu
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (kategori == "Favorilerim") {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = if (isSelected) Color.White else Color(0xFFEF4444), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(text = kategori, color = if (isSelected) Color.White else Color(0xFF64748B), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            if (yukleniyor) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF2563EB)) }
            } else if (filtrelenmisUrunler.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(if (seciliKategori == "Favorilerim") "Henüz favoriye eklediğiniz ürün yok." else "Aradığınız kriterlere uygun ürün bulunamadı.", color = Color(0xFF64748B))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(filtrelenmisUrunler) { urun ->
                        val isFavorite = favoriUrunIds.contains(urun.id)
                        MusteriUrunKarti(
                            urun = urun,
                            iskontoOrani = iskontoOrani,
                            isFavorite = isFavorite, // YENİ
                            onFavoriteClick = { catalogViewModel.favoriDurumuDegistir(urun.id) }, // YENİ
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (urun.gorselUrl.isNotEmpty()) {
                    AsyncImage(model = urun.gorselUrl, contentDescription = urun.ad, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Image, contentDescription = "Yok", tint = Color(0xFF94A3B8), modifier = Modifier.size(24.dp))
                }

                if (iskontoOrani > 0) {
                    Box(modifier = Modifier.align(Alignment.BottomStart).background(Color(0xFFEF4444), RoundedCornerShape(topEnd = 8.dp, bottomStart = 12.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("%${iskontoOrani.toInt()}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(28.dp).background(Color.White.copy(alpha = 0.8f), CircleShape).clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (isFavorite) Color(0xFFEF4444) else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = urun.ad, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Kategori: ${urun.kategori.ifEmpty { "Belirtilmemiş" }}", fontSize = 11.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Min. Alım: ${urun.minAlimMiktari} Adet", fontSize = 11.sp, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(6.dp))

                if (iskontoOrani > 0) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "$normalFiyatStr ₺", fontSize = 12.sp, color = Color(0xFF94A3B8), textDecoration = TextDecoration.LineThrough)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "$indirimliFiyatStr ₺", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF16A34A))
                    }
                } else {
                    Text(text = "$normalFiyatStr ₺", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF2563EB))
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSepeteEkle,
                modifier = Modifier.size(42.dp).background(Color(0xFF2563EB), RoundedCornerShape(10.dp))
            ) { Icon(Icons.Default.AddShoppingCart, contentDescription = "Sepete Ekle", tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
    }
}