package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.AppleRed
import com.example.myapplication.viewmodel.SettingsAndUpdateViewModel
import com.example.myapplication.viewmodel.ShiftViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Sales : Screen("sales", "Ventas", Icons.Default.PointOfSale)
    object Tables : Screen("tables", "Mesas", Icons.Default.TableBar)
    object Customers : Screen("customers", "Clientes", Icons.Default.People)
    object Inventory : Screen("inventory", "Inventario", Icons.Default.Inventory)
    object Reports : Screen("reports", "Reportes", Icons.Default.Assessment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosHomeScreen(
    onLogoutGlobal: () -> Unit
) {
    val shiftViewModel: ShiftViewModel = hiltViewModel()
    val settingsViewModel: SettingsAndUpdateViewModel = hiltViewModel()
    
    val navController = rememberNavController()
    val currentEmployee by shiftViewModel.currentEmployee.collectAsState()
    val businessSettings by settingsViewModel.businessSettings.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }

    if (currentEmployee == null) {
        PinLockScreen()
        return
    }

    val isAdmin = currentEmployee?.role == "ADMIN"

    val items = if (isAdmin) {
        listOf(
            Screen.Sales,
            Screen.Tables,
            Screen.Customers,
            Screen.Inventory,
            Screen.Reports
        )
    } else {
        listOf(
            Screen.Sales,
            Screen.Tables,
            Screen.Customers
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = businessSettings.businessName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentEmployee!!.name} • ${if (isAdmin) "Administrador" else "Cajero"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isAdmin) {
                            showSettingsSheet = true
                        } else {
                            // Si es cajero, solicitar PIN de Administrador
                            shiftViewModel.verifyAdminPin("1234") { isOk ->
                                if (isOk) showSettingsSheet = true
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes del Negocio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { shiftViewModel.logoutEmployee() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar Sesión",
                            tint = AppleRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
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
            composable(Screen.Sales.route) { SalesCatalogScreen() }
            composable(Screen.Tables.route) { TablesScreen() }
            composable(Screen.Customers.route) { CustomersScreen() }
            composable(Screen.Inventory.route) { InventoryScreen() }
            composable(Screen.Reports.route) { ReportsScreen() }
        }
    }

    if (showSettingsSheet) {
        SettingsScreen(
            onDismiss = { showSettingsSheet = false },
            onLogoutGlobal = onLogoutGlobal
        )
    }
}