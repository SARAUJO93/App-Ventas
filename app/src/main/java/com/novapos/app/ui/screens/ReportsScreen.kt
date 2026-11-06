package com.novapos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.novapos.app.data.local.TransactionEntity
import com.novapos.app.model.Order
import com.novapos.app.model.PaymentMethod
import com.novapos.app.ui.components.AppleButton
import com.novapos.app.ui.components.AppleCard
import com.novapos.app.ui.theme.AppleBlue
import com.novapos.app.ui.theme.AppleGreen
import com.novapos.app.ui.theme.AppleOrange
import com.novapos.app.ui.theme.AppleRed
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    ordersViewModel: com.novapos.app.viewmodel.OrdersAndReportsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    shiftViewModel: com.novapos.app.viewmodel.ShiftViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val orders by ordersViewModel.orders.collectAsState()
    val activeShift by shiftViewModel.activeShift.collectAsState()
    val transactions by shiftViewModel.transactions.collectAsState()

    var selectedOrderForReceipt by remember { mutableStateOf<Order?>(null) }
    var showShiftModal by remember { mutableStateOf(false) }
    var showTransactionSheet by remember { mutableStateOf(false) }
    var transactionTypeToAdd by remember { mutableStateOf("INCOME") }

    val totalSales = orders.sumOf { it.total }
    val totalOrders = orders.size
    val averageTicket = if (totalOrders > 0) totalSales / totalOrders else 0.0

    val manualIncomes = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val manualExpenses = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val netProfit = totalSales + manualIncomes - manualExpenses

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
        val context = androidx.compose.ui.platform.LocalContext.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reportes y Finanzas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {
                    com.novapos.app.util.ReportExporter.exportOrdersToCsv(context, orders)
                }) {
                    Icon(Icons.Default.Share, contentDescription = "Exportar a Excel / CSV", tint = AppleBlue)
                }
                OutlinedButton(onClick = { showShiftModal = true }) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (activeShift == null) "Apertura Caja" else "Corte Z")
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Estado del Turno de Caja
            item {
                AppleCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (activeShift != null) "Turno Activo: ${activeShift!!.cashierName}" else "Turno Cerrado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (activeShift != null) "Fondo: \$${String.format(Locale.getDefault(), "%.2f", activeShift!!.initialCash)} • Esperado: \$${String.format(Locale.getDefault(), "%.2f", activeShift!!.expectedCash)}" else "Abre turno de caja para realizar arqueos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(if (activeShift != null) AppleGreen else AppleRed)
                        )
                    }
                }
            }

            // Resumen Financiero Real: Ingresos vs Gastos
            item {
                AppleCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Balance General", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Ganancia Neta: \$${String.format(Locale.getDefault(), "%.2f", netProfit)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) AppleGreen else AppleRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = AppleGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ingresos: \$${String.format(Locale.getDefault(), "%.2f", totalSales + manualIncomes)}", style = MaterialTheme.typography.bodyMedium, color = AppleGreen, fontWeight = FontWeight.SemiBold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = AppleRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gastos: \$${String.format(Locale.getDefault(), "%.2f", manualExpenses)}", style = MaterialTheme.typography.bodyMedium, color = AppleRed, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                transactionTypeToAdd = "INCOME"
                                showTransactionSheet = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("↑ Ingreso")
                        }

                        Button(
                            onClick = {
                                transactionTypeToAdd = "EXPENSE"
                                showTransactionSheet = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppleRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("↓ Gasto")
                        }
                    }
                }
            }

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

            // Movimientos Manuales de Caja (Ingresos / Gastos)
            item {
                Text(
                    text = "Movimientos Manuales de Caja",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Text(
                        text = "No se han registrado ingresos o gastos manuales",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRowCard(transaction)
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

    if (showTransactionSheet) {
        AddTransactionSheet(
            type = transactionTypeToAdd,
            onDismiss = { showTransactionSheet = false },
            onConfirm = { type, amount, category, note ->
                shiftViewModel.addTransaction(type, amount, category, note)
                showTransactionSheet = false
            }
        )
    }

    if (showShiftModal) {
        CashShiftModal(
            onDismiss = { showShiftModal = false }
        )
    }

    if (selectedOrderForReceipt != null) {
        TicketReceiptModal(
            order = selectedOrderForReceipt!!,
            onDismiss = { selectedOrderForReceipt = null }
        )
    }
}

@Composable
fun TransactionRowCard(transaction: TransactionEntity) {
    val isIncome = transaction.type == "INCOME"
    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(transaction.timestamp)

    AppleCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isIncome) AppleGreen.copy(alpha = 0.15f) else AppleRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isIncome) AppleGreen else AppleRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.category + if (transaction.note.isNotEmpty()) " (${transaction.note})" else "",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "${if (isIncome) "+" else "-"}\$${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
                fontWeight = FontWeight.Bold,
                color = if (isIncome) AppleGreen else AppleRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    type: String,
    onDismiss: () -> Unit,
    onConfirm: (type: String, amount: Double, category: String, note: String) -> Unit
) {
    val isIncome = type == "INCOME"
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(if (isIncome) "Inyección de Caja" else "Insumos / Proveedor") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val incomeCategories = listOf("Inyección de Caja", "Abono Especial", "Otros Ingresos")
    val expenseCategories = listOf("Insumos / Proveedor", "Servicios (Luz/Agua)", "Sueldos / Propina", "Retiro de Caja", "Otro Gasto")
    val categories = if (isIncome) incomeCategories else expenseCategories

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isIncome) "Registrar Ingreso Manual" else "Registrar Gasto de Caja",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isIncome) AppleGreen else AppleRed
            )

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = AppleRed, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Categoría del Movimiento", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Nota / Justificación (Opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppleButton(
                text = if (isIncome) "Guardar Ingreso" else "Guardar Gasto",
                backgroundColor = if (isIncome) AppleGreen else AppleRed,
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    when {
                        amount == null || amount <= 0 -> errorMessage = "Ingresa un monto válido mayor a 0"
                        else -> onConfirm(type, amount, selectedCategory, note.trim())
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
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
