package com.novapos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.novapos.app.data.local.TableItemEntity
import com.novapos.app.model.Product
import com.novapos.app.model.Table
import com.novapos.app.model.TableStatus
import com.novapos.app.ui.components.AppleButton
import com.novapos.app.ui.components.AppleCard
import com.novapos.app.ui.theme.AppleGreen
import com.novapos.app.ui.theme.AppleOrange
import com.novapos.app.ui.theme.AppleRed
import com.novapos.app.viewmodel.CartViewModel
import com.novapos.app.viewmodel.InventoryViewModel
import com.novapos.app.viewmodel.TablesViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(
    tablesViewModel: TablesViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    cartViewModel: CartViewModel = hiltViewModel()
) {
    val tables by tablesViewModel.tables.collectAsState()
    val products by inventoryViewModel.products.collectAsState()

    var selectedTable by remember { mutableStateOf<Table?>(null) }
    var showComandaSheet by remember { mutableStateOf(false) }
    var showAddProductsSheet by remember { mutableStateOf(false) }
    var showCartSheetForCheckout by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mapa de Mesas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            val occupiedCount = tables.count { it.status != TableStatus.AVAILABLE }
            Text(
                text = "$occupiedCount / ${tables.size} Ocupadas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tables) { table ->
                TableCard(
                    table = table,
                    onClick = {
                        selectedTable = table
                        showComandaSheet = true
                    }
                )
            }
        }
    }

    if (showComandaSheet && selectedTable != null) {
        val currentTable = tables.find { it.id == selectedTable!!.id } ?: selectedTable!!
        val tableItems by tablesViewModel.getTableItems(currentTable.id).collectAsState(initial = emptyList())

        TableComandaBottomSheet(
            table = currentTable,
            items = tableItems,
            onDismiss = { showComandaSheet = false },
            onUpdateStatus = { newStatus ->
                tablesViewModel.updateTableStatus(currentTable, newStatus)
            },
            onAddProductsClick = {
                showAddProductsSheet = true
            },
            onRemoveItem = { productId ->
                tablesViewModel.removeItemFromTable(currentTable.id, productId)
            },
            onAddItem = { productId ->
                val prod = products.find { it.id == productId }
                if (prod != null) {
                    tablesViewModel.addItemToTable(currentTable.id, prod)
                }
            },
            onClearOrder = {
                tablesViewModel.clearTableOrder(currentTable.id)
                showComandaSheet = false
            },
            onCheckoutClick = {
                cartViewModel.transferTableOrderToCart(currentTable.id) {
                    showComandaSheet = false
                    showCartSheetForCheckout = true
                }
            }
        )
    }

    if (showAddProductsSheet && selectedTable != null) {
        val currentTable = selectedTable!!
        AddProductsToTableSheet(
            products = products,
            onDismiss = { showAddProductsSheet = false },
            onSelectProduct = { product ->
                tablesViewModel.addItemToTable(currentTable.id, product)
            }
        )
    }

    if (showCartSheetForCheckout) {
        val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        CartBottomSheet(
            sheetState = cartSheetState,
            onDismiss = { showCartSheetForCheckout = false }
        )
    }
}

@Composable
fun TableCard(
    table: Table,
    onClick: () -> Unit
) {
    val (statusColor, statusText) = when (table.status) {
        TableStatus.AVAILABLE -> AppleGreen to "Disponible"
        TableStatus.OCCUPIED -> AppleOrange to "Ocupada"
        TableStatus.WAITING_BILL -> AppleRed to "Esperando Cuenta"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = table.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Column {
                if (table.status != TableStatus.AVAILABLE) {
                    Text(
                        text = "Total: \$${String.format(Locale.getDefault(), "%.2f", table.currentOrderTotal)}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Tiempo: ${table.occupancyTimeMinutes} min",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableComandaBottomSheet(
    table: Table,
    items: List<TableItemEntity>,
    onDismiss: () -> Unit,
    onUpdateStatus: (TableStatus) -> Unit,
    onAddProductsClick: () -> Unit,
    onRemoveItem: (productId: String) -> Unit,
    onAddItem: (productId: String) -> Unit,
    onClearOrder: () -> Unit,
    onCheckoutClick: () -> Unit
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = table.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Estado actual: ${table.status.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "\$${String.format(Locale.getDefault(), "%.2f", table.currentOrderTotal)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text("Estado de la Mesa", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = table.status == TableStatus.AVAILABLE,
                    onClick = { onUpdateStatus(TableStatus.AVAILABLE) },
                    label = { Text("Disponible") },
                    leadingIcon = if (table.status == TableStatus.AVAILABLE) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = AppleGreen) }
                    } else null
                )
                FilterChip(
                    selected = table.status == TableStatus.OCCUPIED,
                    onClick = { onUpdateStatus(TableStatus.OCCUPIED) },
                    label = { Text("Ocupada") },
                    leadingIcon = if (table.status == TableStatus.OCCUPIED) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = AppleOrange) }
                    } else null
                )
                FilterChip(
                    selected = table.status == TableStatus.WAITING_BILL,
                    onClick = { onUpdateStatus(TableStatus.WAITING_BILL) },
                    label = { Text("Pedir Cuenta") },
                    leadingIcon = if (table.status == TableStatus.WAITING_BILL) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = AppleRed) }
                    } else null
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comanda / Consumo (${items.sumOf { it.quantity }})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = onAddProductsClick) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar Productos")
                }
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "La comanda de esta mesa está vacía",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        AppleCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.productName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "\$${String.format(Locale.getDefault(), "%.2f", item.unitPrice)} c/u",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onRemoveItem(item.productId) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Restar")
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    IconButton(
                                        onClick = { onAddItem(item.productId) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Sumar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (items.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearOrder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppleRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar")
                    }
                }

                AppleButton(
                    text = "Cobrar Mesa",
                    onClick = onCheckoutClick,
                    enabled = items.isNotEmpty(),
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductsToTableSheet(
    products: List<Product>,
    onDismiss: () -> Unit,
    onSelectProduct: (Product) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Seleccionar Producto para Mesa",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products) { product ->
                    AppleCard(
                        modifier = Modifier.clickable {
                            onSelectProduct(product)
                            onDismiss()
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(product.colorHex))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = product.name, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = product.category,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "\$${String.format(Locale.getDefault(), "%.2f", product.price)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
