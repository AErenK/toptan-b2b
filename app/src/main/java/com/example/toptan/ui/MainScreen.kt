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

    val sharedCartViewModel: CartViewModel = viewModel()

    Scaffold(
        bottomBar = {
            // Toptancıya ait tüm ekranlarda ve giriş/kayıt ekranlarında alt menüyü gizle
            val gizlenecekEkranlar = listOf("login", "register", "toptanci_home", "toptanci_siparisler")

            if (currentRoute !in gizlenecekEkranlar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(paddingValues)
        ) {

            // 1. Giriş Ekranı
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { rol ->
                        // Rol kontrolü: Toptancıysa panele, müşteriyse keşfete
                        val hedefEkran = if (rol == "toptanci") "toptanci_home" else "home"
                        navController.navigate(hedefEkran) {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("register")
                    }
                )
            }

            // 2. Yeni Kayıt Ekranı
            composable("register") {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = { rol ->
                        val hedefEkran = if (rol == "toptanci") "toptanci_home" else "home"
                        navController.navigate(hedefEkran) {
                            popUpTo(0) { inclusive = true } // Geçmişi tamamen sil
                        }
                    }
                )
            }

            // 3. Toptancı Paneli Ana Ekranı
            // 3. Toptancı Paneli Ana Ekranı
            composable("toptanci_home") {
                ToptanciHomeScreen(
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSiparisler = {
                        navController.navigate("toptanci_siparisler") // YENİ: Siparişlere git
                    }
                )
            }

            // YENİ: Toptancı Gelen Siparişler Ekranı
            composable("toptanci_siparisler") {
                ToptanciSiparisScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 4. Müşteri (Dükkan) Keşfet Ekranı
            composable("home") {
                HomeScreen(
                    onNavigateToCatalog = { toptanciId ->
                        // Toptancı ID'sini rota ile yolluyoruz
                        navController.navigate("catalog/$toptanciId")
                    }
                )
            }

            // 5. Katalog Ekranı (Değişen kısım)
            composable("catalog/{toptanciId}") { backStackEntry ->
                // Tıklanan toptancının ID'sini yakalıyoruz
                val tiklananToptanciId = backStackEntry.arguments?.getString("toptanciId") ?: ""

                CatalogScreen(
                    toptanciId = tiklananToptanciId,
                    cartViewModel = sharedCartViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Diğer Müşteri Ekranları
            composable("orders") { OrdersScreen() }
            composable("cart") {
                CartScreen(viewModel = sharedCartViewModel)
            }
            composable("profile") {
                ProfileScreen(
                    onLogoutClick = {
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
            selected = currentRoute == "home" || (currentRoute?.startsWith("catalog") == true),
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