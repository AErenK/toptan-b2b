package com.example.toptan.ui.musteri

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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.toptan.ui.ToptanciUrunEkleScreen
import com.example.toptan.ui.auth.LoginScreen
import com.example.toptan.ui.auth.RegisterScreen
import com.example.toptan.ui.toptanci.ToptanciHomeScreen
import com.example.toptan.ui.toptanci.ToptanciKatalogScreen
import com.example.toptan.ui.toptanci.ToptanciSiparisScreen
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
            // Toptancı sayfalarında ve Auth ekranlarında alt menüyü gizliyoruz
            val gizlenecekEkranlar = listOf("login", "register", "toptanci_home", "toptanci_siparisler", "toptanci_urun_ekle", "toptanci_katalog")
            if (currentRoute !in gizlenecekEkranlar) {
                BottomNavigationBar(navController = navController, sepetUrunSayisi = sepetUrunSayisi)
            }
        },
        containerColor = Color(0xFFF8FAFC)
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
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToSiparisler = { navController.navigate("toptanci_siparisler") },
                    onNavigateToUrunEkle = { navController.navigate("toptanci_urun_ekle") },
                    onNavigateToKatalog = { navController.navigate("toptanci_katalog") }
                )
            }
            composable("toptanci_katalog") {
                ToptanciKatalogScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("toptanci_urun_ekle") {
                ToptanciUrunEkleScreen(
                    onBackClick = { navController.popBackStack() }
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
fun BottomNavigationBar(navController: NavController, sepetUrunSayisi: Int) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 6.dp
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
            label = { Text("Keşfet", fontSize = 11.sp) },
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
            label = { Text("Siparişler", fontSize = 11.sp) },
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

        NavigationBarItem(
            icon = {
                BadgedBox(
                    badge = {
                        if (sepetUrunSayisi > 0) {
                            Badge(containerColor = Color(0xFFEF4444), contentColor = Color.White) {
                                Text(text = sepetUrunSayisi.toString())
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Sepet")
                }
            },
            label = { Text("Sepet", fontSize = 11.sp) },
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
            label = { Text("Profil", fontSize = 11.sp) },
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