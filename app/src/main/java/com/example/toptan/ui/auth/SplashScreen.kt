package com.example.toptan.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: (String) -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    // 1. Logo Büyüme ve Parlama Animasyonu
    val scaleLogo by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "LogoScale"
    )

    // 2. Nabız (Pulse) Efekti İçin Sonsuz Animasyon
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // 3. Yazıların Aşağıdan Süzülerek Gelmesi
    val textOffset by animateDpAsState(
        targetValue = if (startAnimation) 0.dp else 40.dp,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "TextOffset"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 400),
        label = "TextAlpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2200) // Animasyonun tadını çıkarmak için süreyi biraz uzattık (2.2 saniye)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("kullanicilar").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val rol = document.getString("rol") ?: "musteri"
                    onNavigateToHome(rol)
                }
                .addOnFailureListener { onNavigateToLogin() }
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF020617)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animasyonlu Logo Alanı
            Box(contentAlignment = Alignment.Center) {
                // Arkadaki Dalgıç (Pulse) Çemberi
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha)
                        .background(Color(0xFF3B82F6), CircleShape)
                )
                // Ana İkon Çemberi
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scaleLogo)
                        .background(
                            brush = Brush.linearGradient(colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(54.dp))
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Aşağıdan Kayan Metinler
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .offset(y = textOffset)
                    .alpha(textAlpha)
            ) {
                Text(
                    text = "TOPTAN B2B",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PREMIUM TİCARET PLATFORMU",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}