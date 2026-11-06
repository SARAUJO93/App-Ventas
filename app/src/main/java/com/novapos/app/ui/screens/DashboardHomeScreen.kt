package com.novapos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.novapos.app.model.Order
import com.novapos.app.model.Product
import com.novapos.app.ui.components.AppleCard
import com.novapos.app.ui.theme.AppleBlue
import com.novapos.app.ui.theme.AppleGreen
import com.novapos.app.ui.theme.AppleOrange
import com.novapos.app.ui.theme.AppleRed
import com.novapos.app.viewmodel.InventoryViewModel
import com.novapos.app.viewmodel.OrdersAndReportsViewModel
import com.novapos.app.viewmodel.SettingsAndUpdateViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardHomeScreen(
    settingsViewModel: SettingsAndUpdateViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    ordersViewModel: OrdersAndReportsViewModel = hiltViewModel(),
    onNavigateToProducts: () -> Unit = {},
    onNavigateToNewProduct: () -> Unit = {},
    onNavigateToSale: () -> Unit = {},
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToEmployees: () -> Unit = {},
    onNavigateToTables: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val settings by settingsViewModel.businessSettings.collectAsState()
    val products by inventoryViewModel.products.collectAsState()
    val orders by ordersViewModel.orders.collectAsState()

    val totalSales = orders.sumOf { it.total }
    val totalOrders = orders.size
    val averageTicket = if (totalOrders > 0) totalSales / totalOrders else 0.0
    val inventoryValue = products.sumOf { it.price * it.stock }

    val inStockCount = products.count { it.stock > 10 }
    val lowStockCount = products.count { it.stock in 1..10 }
    val outOfStockCount = products.count { it.stock == 0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F7FA))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Banner Superior: ¿Necesitas Ayuda?
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AppleOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛟", fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Necesitas Ayuda?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Nuestro equipo está listo para aclarar dudas y guiarte en Nova POS.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Text("Contactar a soporte >", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Sección 1: Reporte de Ventas con Gráfico Semanal y KPIs
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Reporte de ventas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(">", color = Color.Gray, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Días de la semana
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = listOf("30 Oct", "31 Oct", "1 Nov", "2 Nov", "3 Nov", "4 Nov", "5 Nov")
                    days.forEach { day ->
                        Text(text = day, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid 2x2 KPIs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardKpiCard(
                        title = "Ventas netas",
                        value = "\$${String.format(Locale.US, "%.2f", totalSales)}",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardKpiCard(
                        title = "Ganancia",
                        value = "\$${String.format(Locale.US, "%.2f", totalSales * 0.4)}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardKpiCard(
                        title = "Ticket promedio",
                        value = "\$${String.format(Locale.US, "%.2f", averageTicket)}",
                        modifier = Modifier.weight(1f)
                    )
                    DashboardKpiCard(
                        title = "Transacciones",
                        value = "$totalOrders",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Sección 2: Valor de Inventario y Estados de Stock
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Valor de inventario", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            text = "\$${String.format(Locale.US, "%.2f", inventoryValue)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(">", color = Color.Gray, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Opciones en inventario", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                StockStatusRow(color = AppleGreen, label = "Disponible", count = inStockCount)
                StockStatusRow(color = AppleOrange, label = "Mínimo", count = lowStockCount)
                StockStatusRow(color = AppleRed, label = "Agotado", count = outOfStockCount)
                StockStatusRow(color = Color.Gray, label = "Sin manejo de stock", count = 0)
            }
        }

        // Sección 3: Pedidos sin Completar
        item {
            AppleCard {
                Text("Pedidos sin Completar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "0",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🕒 Pendientes (0)", style = MaterialTheme.typography.bodyMedium)
                    Text("\$0.00", fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🔵 En curso (0)", style = MaterialTheme.typography.bodyMedium)
                    Text("\$0.00", fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🟢 Pagados (0)", style = MaterialTheme.typography.bodyMedium)
                    Text("\$0.00", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver pedidos")
                }
            }
        }

        // Sección 4: Módulo de Productos
        item {
            DashboardActionCard(
                title = "Productos",
                description = "Crea productos ilimitados con fotos, variantes y precios.",
                buttonText = "+ Nuevo producto",
                iconText = "📦",
                onClick = onNavigateToNewProduct
            )
        }

        // Sección 5: Módulo de Clientes & Lealtad
        item {
            DashboardActionCard(
                title = "Clientes",
                description = "¡Nuevo Programa de Lealtad! Importa tus clientes y otorga puntos para futuras compras.",
                buttonText = "+ Nuevo cliente",
                iconText = "👤",
                onClick = onNavigateToCustomers
            )
        }

        // Sección 6: Venta a Crédito (Fiado)
        item {
            AppleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Venta a crédito", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1. Crea un pedido", style = MaterialTheme.typography.bodySmall)
                        Text("2. Selecciona cliente", style = MaterialTheme.typography.bodySmall)
                        Text("3. Forma de pago \"A crédito\"", style = MaterialTheme.typography.bodySmall)
                        Text("4. ¡Listo, todo en orden!", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToSale,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
                        ) {
                            Text("Nueva Venta", fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AppleBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💳", fontSize = 36.sp)
                    }
                }
            }
        }

        // Sección 7: Empleados
        item {
            DashboardActionCard(
                title = "Empleados",
                description = "¿Bloquear permisos? Configura permisos y controla las ventas de cada uno.",
                buttonText = "+ Nuevo usuario",
                iconText = "👥",
                onClick = onNavigateToEmployees
            )
        }

        // Sección 8: Mesas
        item {
            DashboardActionCard(
                title = "Mesas",
                description = "¡Atiende mesas! Gestiona múltiples mesas y envía comandas a cocina.",
                buttonText = "Ir a mesas",
                iconText = "🪑",
                onClick = onNavigateToTables
            )
        }

        // Sección 9: Catálogo en Línea
        item {
            DashboardActionCard(
                title = "Activa tu Catálogo en Línea",
                description = "Recibe pedidos desde tu propia página web.",
                buttonText = "Activar Catálogo",
                iconText = "🌐",
                onClick = { }
            )
        }

        // Sección 10: Importar desde Excel
        item {
            DashboardActionCard(
                title = "Importar tu inventario desde Excel",
                description = "Envía un mensaje y te ayudaremos a tener todo listo.",
                buttonText = "Importar inventario",
                iconText = "📊",
                onClick = { }
            )
        }
    }
}

@Composable
fun DashboardKpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StockStatusRow(
    color: Color,
    label: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(text = "$count", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DashboardActionCard(
    title: String,
    description: String,
    buttonText: String,
    iconText: String,
    onClick: () -> Unit
) {
    AppleCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
                ) {
                    Text(buttonText, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AppleBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(iconText, fontSize = 32.sp)
            }
        }
    }
}