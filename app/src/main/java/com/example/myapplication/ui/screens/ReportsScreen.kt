package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Order
import com.example.myapplication.model.PaymentMethod
import com.example.myapplication.ui.components.AppleCard
import com.example.myapplication.ui.theme.AppleBlue
import com.example.myapplication.ui.theme.AppleGreen
import com.example.myapplication.ui.theme.AppleOrange
import com.example.myapplication.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: PosViewModel) {
    val orders by viewModel.orders.collectAsState()
    var selectedOrderForReceipt by remember { mutableStateOf<Order?>(null) }

    val totalSales = orders.sumOf { it.total }
    val totalOrders = orders.size
    val averageTicket = if (totalOrders > 0) totalSales / totalOrders else 0.0

    val salesByPaymentMethod = PaymentMethod.entries.associateWith { method ->
        orders.filter { it.paymentMethod == method }.sumOf { it.total }
    }

    val topProducts = orders.flatMap { it.items }
        .groupBy { it.product.name }
        .mapValues { entry -> entry.value.sumOf { it.quantity } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Reportes y Estadísticas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // KPI Summary Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiCard(
                        title = "Ventas Totales",
                        value = "\$${String.format(Locale.getDefault(), "%.2f", totalSales)}",
                        icon = Icons.Default.TrendingUp,
                        iconColor = AppleGreen,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Órdenes",
                        value = "$totalOrders",
                        icon = Icons.Default.ReceiptLong,
                        iconColor = AppleBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                KpiCard(
                    title = "Ticket Promedio",
                    value = "\$${String.format(Locale.getDefault(), "%.2f", averageTicket)}",
                    icon = Icons.Default.BarChart,
                    iconColor = AppleOrange,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Ventas por Método de Pago
            item {
                AppleCard {
                    Text(
                        text = "Ventas por Método de Pago",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val maxSales = salesByPaymentMethod.values.maxOrNull() ?: 1.0

                    salesByPaymentMethod.forEach { (method, amount) ->
                        val name = when (method) {
                            PaymentMethod.CASH -> "Efectivo"
                            PaymentMethod.CARD -> "Tarjeta"
                            PaymentMethod.TRANSFER -> "Transferencia"
                            PaymentMethod.CREDIT -> "Fiado / Crédito"
                        }
                        val progress = if (maxSales > 0) (amount / maxSales).toFloat() else 0f

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = "\$${String.format(Locale.getDefault(), "%.2f", amount)}",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            // Top Productos Más Vendidos
            item {
                AppleCard {
                    Text(
                        text = "Top Productos Más Vendidos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (topProducts.isEmpty()) {
                        Text(
                            text = "Aún no hay ventas registradas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        topProducts.forEachIndexed { index, (productName, quantity) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = productName, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    text = "$quantity unids.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Historial de Ventas Recientes
            item {
                Text(
                    text = "Historial de Ventas Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (orders.isEmpty()) {
                item {
                    Text(
                        text = "No se han realizado ventas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(orders, key = { it.id }) { order ->
                    OrderHistoryCard(
                        order = order,
                        onClick = {
                            selectedOrderForReceipt = order
                        }
                    )
                }
            }
        }
    }

    if (selectedOrderForReceipt != null) {
        TicketReceiptModal(
            order = selectedOrderForReceipt!!,
            onDismiss = { selectedOrderForReceipt = null }
        )
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    AppleCard(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun OrderHistoryCard(
    order: Order,
    onClick: () -> Unit
) {
    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(order.date)
    val methodText = when (order.paymentMethod) {
        PaymentMethod.CASH -> "Efectivo"
        PaymentMethod.CARD -> "Tarjeta"
        PaymentMethod.TRANSFER -> "Transferencia"
        PaymentMethod.CREDIT -> "Fiado (${order.customerName ?: "Cliente"})"
        null -> "Efectivo"
    }

    AppleCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Orden #${order.id.takeLast(6)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$formattedDate • $methodText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "\$${String.format(Locale.getDefault(), "%.2f", order.total)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}