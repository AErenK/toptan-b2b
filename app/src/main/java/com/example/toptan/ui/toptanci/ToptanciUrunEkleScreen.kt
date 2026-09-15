package com.example.toptan.ui

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.toptan.viewmodel.toptanci.ToptanciViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToptanciUrunEkleScreen(
    viewModel: ToptanciViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    var urunAdi by remember { mutableStateOf("") }
    var fiyat by remember { mutableStateOf("") }
    var minAlim by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }
    var gorselUri by remember { mutableStateOf<Uri?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var secilenKategori by remember { mutableStateOf("Gıda") }
    val kategoriler = listOf("Gıda", "İçecek", "Temizlik", "Kozmetik", "Kırtasiye", "Teknoloji", "Diğer")

    val mesaj by viewModel.mesaj.collectAsState()
    val context = LocalContext.current
    var csvDialogAcik by remember { mutableStateOf(false) }
    var birDahaGosterme by remember { mutableStateOf(false) }
    val sharedPrefs = context.getSharedPreferences("ToptanAyarlar", Context.MODE_PRIVATE)
    val uyariyiGizle = sharedPrefs.getBoolean("uyariyi_gizle_csv", false)

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> gorselUri = uri }

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val satirlar = context.contentResolver.openInputStream(it)?.bufferedReader()?.useLines { lines -> lines.toList() }
                if (satirlar != null && satirlar.size > 1) {
                    viewModel.topluUrunEkle(satirlar)
                } else {
                    Toast.makeText(context, "Dosya boş veya geçersiz.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Dosya okuma hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(mesaj) {
        if (mesaj?.contains("başarıyla") == true) {
            delay(2000)
            urunAdi = ""; fiyat = ""; minAlim = ""; stok = ""; gorselUri = null; secilenKategori = "Gıda"
            viewModel.mesajiTemizle()
            onBackClick()
        } else if (mesaj != null) {
            delay(3000)
            viewModel.mesajiTemizle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Yeni Ürün Ekle", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                        Text("Kataloğa toptan ürün ekleme formu", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 4.dp).size(38.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f), shape = CircleShape)
                    ) { Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Brush.verticalGradient(colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f), Color.Transparent))))

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        if (uyariyiGizle) {
                            csvLauncher.launch("*/*")
                        } else {
                            csvDialogAcik = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("CSV Dosyası ile Toplu Yükle", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surface).clickable { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (gorselUri != null) {
                        AsyncImage(model = gorselUri, contentDescription = "Görsel", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Box(modifier = Modifier.size(54.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), shape = CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Image, contentDescription = "Seç", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Ürün Fotoğrafı Eklemek İçin Dokunun", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("PNG veya JPG formatında", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = urunAdi, onValueChange = { urunAdi = it }, label = { Text("Ürün Adı", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface), singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = secilenKategori,
                        onValueChange = {
                            secilenKategori = it
                            expanded = true
                        },
                        readOnly = false,
                        label = { Text("Kategori", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        kategoriler.forEach { kategori ->
                            DropdownMenuItem(
                                text = { Text(kategori, color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    secilenKategori = kategori
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = fiyat, onValueChange = { fiyat = it }, label = { Text("Fiyat (₺)", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface), singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = minAlim, onValueChange = { minAlim = it }, label = { Text("Min. Alım", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface), singleLine = true
                    )
                    OutlinedTextField(
                        value = stok, onValueChange = { stok = it }, label = { Text("Stok", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface), singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(visible = !mesaj.isNullOrEmpty(), enter = fadeIn(), exit = fadeOut()) {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = if (mesaj.orEmpty().contains("başarıyla")) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)) {
                        Text(text = mesaj ?: "", color = if (mesaj.orEmpty().contains("başarıyla")) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
                    }
                }

                if (!mesaj.isNullOrEmpty()) Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.urunEkle(urunAdi, fiyat, minAlim, stok, secilenKategori, gorselUri) },
                    modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Ekle", tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kataloğa Ekle", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (csvDialogAcik) {
        AlertDialog(
            onDismissRequest = { csvDialogAcik = false },
            title = { Text("Toplu Yükleme Nasıl Yapılır?", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Column {
                    Text("Excel veya benzeri bir programda ürünlerinizi sırayla sütunlara yazın:\n\n1. Ürün Adı\n2. Fiyat (Örn: 15.50)\n3. Min. Alım Adedi\n4. Stok\n5. Kategori\n6. Görsel Linki (Opsiyonel)\n\nSonrasında 'Farklı Kaydet' diyerek dosyayı '.csv' formatında telefonunuza kaydedin ve buradan seçin.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { birDahaGosterme = !birDahaGosterme }) {
                        Checkbox(
                            checked = birDahaGosterme,
                            onCheckedChange = { birDahaGosterme = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text("Bir daha gösterme", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                Button(
                    onClick = {
                        if (birDahaGosterme) {
                            sharedPrefs.edit().putBoolean("uyariyi_gizle_csv", true).apply()
                        }
                        csvDialogAcik = false
                        csvLauncher.launch("*/*")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Anladım, Dosya Seç", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background) }
            },
            dismissButton = { TextButton(onClick = { csvDialogAcik = false }) { Text("İptal", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) } }
        )
    }
}