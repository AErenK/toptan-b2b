package com.example.toptan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.toptan.ui.musteri.MainScreen
import com.example.toptan.ui.auth.SplashScreen
import com.example.toptan.ui.theme.ToptanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ToptanTheme {

                // YENİ: Splash Screen (Açılış Ekranı) durumunu tutan değişken
                var showSplash by remember { mutableStateOf(true) }

                // Scaffold yerine Surface kullanıyoruz ki ekranları daha rahat kaplasın
                Surface(modifier = Modifier.fillMaxSize()) {

                    if (showSplash) {
                        // 1. Durum: Uygulama ilk açıldığında Splash Screen'i göster
                        SplashScreen(
                            onSplashFinished = {
                                showSplash = false // Süre bitince false yap ve ana ekrana geç
                            }
                        )
                    } else {
                        // 2. Durum: Splash bittikten sonra uygulamanın asıl (MainScreen) ekranını göster
                        MainScreen()
                    }

                }
            }
        }
    }
}