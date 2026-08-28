package com.example.toptan.ui
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Alt menü sekmelerimiz
    val routes = listOf("home", "orders", "cart", "profile")
    val icons = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.ShoppingCart, Icons.Default.Person)
    val labels = listOf("Keşfet", "Siparişler", "Sepet", "Profil")

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                routes.forEachIndexed { index, route ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = labels[index]) },
                        label = { Text(labels[index]) },
                        selected = currentRoute == route,
                        onClick = {
                            navController.navigate(route) {
                                // Geri tuşuna basıldığında yığın (stack) şişmesin diye
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        // Ekranların gösterileceği ana çerçeve (Router)
        NavHost(
            navController = navController,
            startDestination = "home", // Uygulama artık doğrudan Keşfet'ten başlasın
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    onNavigateToCatalog = { navController.navigate("catalog") }
                )
            }
            composable("catalog") {
                CatalogScreen(
                    onBackClick = { navController.popBackStack() } // Geri tuşuna basınca bir önceki sayfaya döner
                )
            }
            composable("orders") { OrdersScreen() }
            composable("cart") { CartScreen() }
            composable("profile") { ProfileScreen() }
        }
    }
}

// Henüz yazılmamış ekranlar için geçici bir tutucu sayfa
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

