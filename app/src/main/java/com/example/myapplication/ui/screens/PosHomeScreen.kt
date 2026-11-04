package com.example.myapplication.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.viewmodel.PosViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Sales : Screen("sales", "Ventas", Icons.Default.PointOfSale)
    object Tables : Screen("tables", "Mesas", Icons.Default.TableBar)
    object Customers : Screen("customers", "Clientes", Icons.Default.People)
    object Inventory : Screen("inventory", "Inventario", Icons.Default.Inventory)
}

@Composable
fun PosHomeScreen() {
    val viewModel: PosViewModel = viewModel()
    val navController = rememberNavController()

    val items = listOf(
        Screen.Sales,
        Screen.Tables,
        Screen.Customers,
        Screen.Inventory
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
            startDestination = Screen.Sales.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Sales.route) { SalesCatalogScreen(viewModel) }
            composable(Screen.Tables.route) { TablesScreen(viewModel) }
            composable(Screen.Customers.route) { CustomersScreen(viewModel) }
            composable(Screen.Inventory.route) { InventoryScreen(viewModel) }
        }
    }
}