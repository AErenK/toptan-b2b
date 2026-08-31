package com.example.toptan.ui

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.navigation.NavController
import com.example.toptan.viewmodel.CartViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // PROJENİN ORTAK SEPET HAFIZASI
    val sharedCartViewModel: CartViewModel = viewModel()

    Scaffold(
        bottomBar = {
            // Eğer "login" ekranında DEĞİLSEK alt menüyü göster
            if (currentRoute != "login") {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login", // Uygulama ilk açıldığında giriş ekranından başlasın
            modifier = Modifier.padding(paddingValues)
        ) {
            // 1. Giriş Ekranı
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        // Giriş başarılı olursa Keşfet'e geç, geri tuşuna basınca bir daha Login'e dönme
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            // 2. Keşfet Ekranı
            composable("home") {
                HomeScreen(
                    onNavigateToCatalog = { navController.navigate("catalog") }
                )
            }

            // 3. Katalog Ekranı
            composable("catalog") {
                CatalogScreen(
                    cartViewModel = sharedCartViewModel, // Hafızayı gönder
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Diğer Ekranlar
            composable("orders") { OrdersScreen() }
            composable("cart") {
                CartScreen(viewModel = sharedCartViewModel) // AYNI hafızayı gönder
            }
            composable("profile") {
                ProfileScreen(
                    onLogoutClick = {
                        // Çıkış yapıldığında Login'e dön ve geri tuşu geçmişini tamamen temizle
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Keşfet") },
            label = { Text("Keşfet") },
            selected = currentRoute == "home" || currentRoute == "catalog",
            onClick = {
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Siparişler") },
            label = { Text("Siparişler") },
            selected = currentRoute == "orders",
            onClick = {
                navController.navigate("orders") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Sepet") },
            label = { Text("Sepet") },
            selected = currentRoute == "cart",
            onClick = {
                navController.navigate("cart") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil") },
            selected = currentRoute == "profile",
            onClick = {
                navController.navigate("profile") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}
