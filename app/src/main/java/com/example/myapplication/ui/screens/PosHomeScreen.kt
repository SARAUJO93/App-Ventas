package com.example.myapplication.ui.screens

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

    val items = listOf(
        Screen.Sales,
        Screen.Tables,
        Screen.Customers,
        Screen.Inventory
    )

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Sales) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentScreen == screen,
                        onClick = { currentScreen = screen },
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
        Modifier.padding(innerPadding).let { _ ->
            when (currentScreen) {
                Screen.Sales -> SalesCatalogScreen(viewModel)
                Screen.Tables -> TablesScreen(viewModel)
                Screen.Customers -> CustomersScreen(viewModel)
                Screen.Inventory -> {
                    // Placeholder para inventario para mantener el archivo más limpio,
                    // ya que es similar a Catálogo pero con edición.
                    Text("Módulo de Inventario en construcción", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}