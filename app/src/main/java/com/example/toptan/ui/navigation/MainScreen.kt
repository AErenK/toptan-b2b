package com.example.toptan.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.toptan.ui.ToptanciUrunEkleScreen
import com.example.toptan.ui.auth.*
import com.example.toptan.ui.toptanci.*
import com.example.toptan.viewmodel.musteri.CartViewModel

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TOPTANCI_HOME = "toptanci_home"
    const val TOPTANCI_KATALOG = "toptanci_katalog"
    const val TOPTANCI_URUN_EKLE = "toptanci_urun_ekle"
    const val TOPTANCI_SIPARISLER = "toptanci_siparisler"
    const val TOPTANCI_MUSTERILER = "toptanci_musteriler"
    const val HOME = "home"
    const val ORDERS = "orders"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val CATALOG = "catalog/{toptanciId}"
    const val PRODUCT_DETAIL = "product_detail/{urunId}"
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val sharedCartViewModel: CartViewModel = viewModel()
    val sepetListesi by sharedCartViewModel.sepet.collectAsState()
    val sepetUrunSayisi = sepetListesi.size

    val bottomBarDestinations = listOf(
        Routes.HOME,
        Routes.ORDERS,
        Routes.CART,
        Routes.PROFILE
    )

    val showBottomBar = bottomBarDestinations.contains(currentRoute) ||
            currentRoute?.startsWith("catalog/") == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController = navController, sepetUrunSayisi = sepetUrunSayisi)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    },
                    onNavigateToHome = { rol ->
                        val hedef = if (rol == "toptanci") Routes.TOPTANCI_HOME else Routes.HOME
                        navController.navigate(hedef) { popUpTo(Routes.SPLASH) { inclusive = true } }
                    }
                )
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = { rol ->
                        val hedef = if (rol == "toptanci") Routes.TOPTANCI_HOME else Routes.HOME
                        navController.navigate(hedef) { popUpTo(Routes.LOGIN) { inclusive = true } }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = { rol ->
                        val hedef = if (rol == "toptanci") Routes.TOPTANCI_HOME else Routes.HOME
                        navController.navigate(hedef) { popUpTo(navController.graph.id) { inclusive = true } }
                    }
                )
            }
            composable(Routes.TOPTANCI_HOME) {
                ToptanciHomeScreen(
                    onLogoutClick = {
                        sharedCartViewModel.sepetiTemizle() // YENİ: Çıkış yaparken sepeti anında yok eder!
                        navController.navigate(Routes.LOGIN) { popUpTo(navController.graph.id) { inclusive = true } }
                    },
                    onNavigateToSiparisler = { navController.navigate(Routes.TOPTANCI_SIPARISLER) },
                    onNavigateToUrunEkle = { navController.navigate(Routes.TOPTANCI_URUN_EKLE) },
                    onNavigateToKatalog = { navController.navigate(Routes.TOPTANCI_KATALOG) },
                    onNavigateToMusteriler = { navController.navigate(Routes.TOPTANCI_MUSTERILER) }
                )
            }
            composable(Routes.TOPTANCI_KATALOG) { ToptanciKatalogScreen(onBackClick = { navController.popBackStack() }) }
            composable(Routes.TOPTANCI_URUN_EKLE) { ToptanciUrunEkleScreen(onBackClick = { navController.popBackStack() }) }
            composable(Routes.TOPTANCI_SIPARISLER) { ToptanciSiparisScreen(onBackClick = { navController.popBackStack() }) }
            composable(Routes.TOPTANCI_MUSTERILER) {
                ToptanciMusterilerScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Routes.HOME) {
                com.example.toptan.ui.musteri.HomeScreen(
                    onNavigateToCatalog = { toptanciId ->
                        navController.navigate("catalog/$toptanciId")
                    },
                    onNavigateToProduct = { urunId ->
                        navController.navigate("product_detail/$urunId")
                    }
                )
            }
            composable(Routes.CATALOG) { backStackEntry ->
                val toptanciId = backStackEntry.arguments?.getString("toptanciId") ?: ""
                _root_ide_package_.com.example.toptan.ui.musteri.CatalogScreen(
                    toptanciId = toptanciId,
                    cartViewModel = sharedCartViewModel,
                    onBackClick = { navController.popBackStack() },
                    onUrunClick = { urunId -> navController.navigate("product_detail/$urunId") }
                )
            }
            composable(Routes.PRODUCT_DETAIL) { backStackEntry ->
                val urunId = backStackEntry.arguments?.getString("urunId") ?: ""
                _root_ide_package_.com.example.toptan.ui.musteri.ProductDetailScreen(
                    urunId = urunId,
                    cartViewModel = sharedCartViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Routes.ORDERS) {
                com.example.toptan.ui.musteri.OrdersScreen(
                    cartViewModel = sharedCartViewModel,
                    onNavigateToCart = { navController.navigate(Routes.CART) }
                )
            }
            composable(Routes.CART) {
                _root_ide_package_.com.example.toptan.ui.musteri.CartScreen(
                    viewModel = sharedCartViewModel
                )
            }
            composable(Routes.PROFILE) {
                _root_ide_package_.com.example.toptan.ui.musteri.ProfileScreen(
                    onLogoutClick = {
                        sharedCartViewModel.sepetiTemizle()
                        navController.navigate(Routes.LOGIN) { popUpTo(navController.graph.id) { inclusive = true } }
                    },
                    onNavigateToSiparislerim = { navController.navigate(Routes.ORDERS) }
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
        containerColor = MaterialTheme.colorScheme.surface, // YENİ: Alt bar dinamik oldu
        tonalElevation = 6.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )

        val navigateTo = { route: String ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Keşfet") },
            label = { Text("Keşfet", fontSize = 11.sp) },
            selected = currentRoute == Routes.HOME || currentRoute?.startsWith("catalog/") == true,
            onClick = { navigateTo(Routes.HOME) },
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Siparişler") },
            label = { Text("Siparişler", fontSize = 11.sp) },
            selected = currentRoute == Routes.ORDERS,
            onClick = { navigateTo(Routes.ORDERS) },
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
            selected = currentRoute == Routes.CART,
            onClick = { navigateTo(Routes.CART) },
            colors = itemColors
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
            label = { Text("Profil", fontSize = 11.sp) },
            selected = currentRoute == Routes.PROFILE,
            onClick = { navigateTo(Routes.PROFILE) },
            colors = itemColors
        )
    }
}