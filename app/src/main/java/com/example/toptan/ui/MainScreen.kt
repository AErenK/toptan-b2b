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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.toptan.viewmodel.CartViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val sharedCartViewModel: CartViewModel = viewModel()
    val sepetListesi by sharedCartViewModel.sepet.collectAsState()
    val sepetUrunSayisi = sepetListesi.size

    Scaffold(
        bottomBar = {
            val gizlenecekEkranlar = listOf("login", "register", "toptanci_home", "toptanci_siparisler")
            if (currentRoute !in gizlenecekEkranlar) {
                BottomNavigationBar(navController = navController, sepetUrunSayisi = sepetUrunSayisi)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { rol ->
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
            composable("register") {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = { rol ->
                        val hedefEkran = if (rol == "toptanci") "toptanci_home" else "home"
                        navController.navigate(hedefEkran) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable("toptanci_home") {
                ToptanciHomeScreen(
                    onLogoutClick = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToSiparisler = {
                        navController.navigate("toptanci_siparisler")
                    }
                )
            }
            composable("toptanci_siparisler") {
                ToptanciSiparisScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("home") {
                HomeScreen(
                    onNavigateToCatalog = { toptanciId ->
                        navController.navigate("catalog/$toptanciId")
                    }
                )
            }
            composable("catalog/{toptanciId}") { backStackEntry ->
                val tiklananToptanciId = backStackEntry.arguments?.getString("toptanciId") ?: ""
                CatalogScreen(
                    toptanciId = tiklananToptanciId,
                    cartViewModel = sharedCartViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
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
fun BottomNavigationBar(navController: NavController, sepetUrunSayisi: Int) { // YENİ: sepetUrunSayisi parametresi eklendi
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color(0xFF2563EB),
            selectedTextColor = Color(0xFF2563EB),
            unselectedIconColor = Color(0xFF64748B),
            unselectedTextColor = Color(0xFF64748B),
            indicatorColor = Color(0xFFDBEAFE)
        )

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
            },
            colors = itemColors
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
            },
            colors = itemColors
        )

        // --- SEPET İKONU (ROZETLİ YAPI) ---
        NavigationBarItem(
            icon = {
                // Material3 BadgedBox bileşeni ile kırmızı baloncuğu ekliyoruz
                BadgedBox(
                    badge = {
                        if (sepetUrunSayisi > 0) {
                            Badge(containerColor = Color.Red, contentColor = Color.White) {
                                Text(text = sepetUrunSayisi.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Sepet")
                }
            },
            label = { Text("Sepet") },
            selected = currentRoute == "cart",
            onClick = {
                navController.navigate("cart") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            colors = itemColors
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
            },
            colors = itemColors
        )
    }
}