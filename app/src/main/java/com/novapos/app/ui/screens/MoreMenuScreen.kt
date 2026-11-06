package com.novapos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.novapos.app.ui.components.AppleCard
import com.novapos.app.ui.theme.AppleBlue
import com.novapos.app.ui.theme.AppleRed
import com.novapos.app.viewmodel.SettingsAndUpdateViewModel
import com.novapos.app.viewmodel.ShiftViewModel

data class MoreMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreMenuScreen(
    settingsViewModel: SettingsAndUpdateViewModel = hiltViewModel(),
    shiftViewModel: ShiftViewModel = hiltViewModel(),
    onNavigateToRoute: (String) -> Unit = {},
    onLogoutGlobal: () -> Unit = {}
) {
    val settings by settingsViewModel.businessSettings.collectAsState()
    val currentEmployee by shiftViewModel.currentEmployee.collectAsState()
    var showSettingsSheet by remember { mutableStateOf(false) }

    val menuItems = listOf(
        MoreMenuItem("Clientes", Icons.Default.Person, "customers"),
        MoreMenuItem("Lealtad", Icons.Default.Loyalty, "loyalty"),
        MoreMenuItem("Compras", Icons.Default.ShoppingBag, "purchases"),
        MoreMenuItem("Proveedores", Icons.Default.LocalShipping, "suppliers"),
        MoreMenuItem("Caja actual", Icons.Default.Inbox, "cash"),
        MoreMenuItem("Reportes", Icons.Default.BarChart, "reports"),
        MoreMenuItem("Tienda en línea", Icons.Default.ShoppingCart, "store"),
        MoreMenuItem("Empleados", Icons.Default.Group, "employees")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F7FA))
            .padding(16.dp)
    ) {
        // Encabezado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = settings.businessName.ifEmpty { "Mi negocio" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${currentEmployee?.name ?: "Usuario"} • ${if (currentEmployee?.role == "ADMIN") "Dueño" else "Cajero"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { showSettingsSheet = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, contentDescription = "Seguridad", tint = Color(0xFF2C3E50))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Banner de Prueba / PRO
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quedan 7 días de prueba",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AppleBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("👑 Mejorar plan", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cuadrícula 2x4
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clickable { onNavigateToRoute(item.route) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = item.title, tint = Color(0xFF334155))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lista inferior de Ajustes y Soporte
        AppleCard {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSettingsSheet = true }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF334155))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Configuraciones", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFF334155))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Ayuda y Soporte", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                }

                HorizontalDivider(color = Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLogoutGlobal() }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = AppleRed)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = AppleRed, modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showSettingsSheet) {
        SettingsScreen(
            onDismiss = { showSettingsSheet = false },
            onLogoutGlobal = onLogoutGlobal
        )
    }
}