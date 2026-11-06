package com.novapos.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.novapos.app.viewmodel.ShiftViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Products : Screen("products", "Productos", Icons.Default.Inventory2)
    object Sale : Screen("sale", "Venta", Icons.Default.AddCircle)
    object Transactions : Screen("transactions", "Transacciones", Icons.Default.ReceiptLong)
    object More : Screen("more", "Más", Icons.Default.Menu)
}

@Composable
fun PosHomeScreen(
    onLogoutGlobal: () -> Unit
) {
    val shiftViewModel: ShiftViewModel = hiltViewModel()
    val navController = rememberNavController()
    val currentEmployee by shiftViewModel.currentEmployee.collectAsState()

    if (currentEmployee == null) {
        PinLockScreen()
        return
    }

    val items = listOf(
        Screen.Home,
        Screen.Products,
        Screen.Sale,
        Screen.Transactions,
        Screen.More
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                DashboardHomeScreen(
                    onNavigateToProducts = { navController.navigate(Screen.Products.route) },
                    onNavigateToNewProduct = { navController.navigate(Screen.Products.route) },
                    onNavigateToSale = { navController.navigate(Screen.Sale.route) },
                    onNavigateToCustomers = { navController.navigate("customers") },
                    onNavigateToEmployees = { navController.navigate("employees") },
                    onNavigateToTables = { navController.navigate("tables") }
                )
            }
            composable(Screen.Products.route) { InventoryScreen() }
            composable(Screen.Sale.route) { SalesCatalogScreen() }
            composable(Screen.Transactions.route) { ReportsScreen() }
            composable(Screen.More.route) {
                MoreMenuScreen(
                    onNavigateToRoute = { route -> navController.navigate(route) },
                    onLogoutGlobal = onLogoutGlobal
                )
            }

            composable("customers") { CustomersScreen() }
            composable("tables") { TablesScreen() }
            composable("employees") { ReportsScreen() }
        }
    }
}