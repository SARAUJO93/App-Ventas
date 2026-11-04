package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.CartItem
import com.example.myapplication.model.Customer
import com.example.myapplication.model.Order
import com.example.myapplication.model.PaymentMethod
import com.example.myapplication.ui.components.AppleButton
import com.example.myapplication.viewmodel.PosViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    viewModel: PosViewModel,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val cartItems by viewModel.cartItems.collectAsState()
    var showCheckout by remember { mutableStateOf(false) }
    var completedOrder by remember { mutableStateOf<Order?>(null) }

    if (completedOrder != null) {
        TicketReceiptModal(
            order = completedOrder!!,
            onDismiss = {
                completedOrder = null
                onDismiss()
            }
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        if (showCheckout) {
            CheckoutView(
                viewModel = viewModel,
                onBack = { showCheckout = false },
                onComplete = { order ->
                    completedOrder = order
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Resumen del Pedido",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            onQuantityChange = { newQ -> viewModel.updateCartItemQuantity(item.product.id, newQ) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("\$${String.format(Locale.getDefault(), "%.2f", viewModel.cartSubtotal)}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("IVA (16%)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("\$${String.format(Locale.getDefault(), "%.2f", viewModel.cartTax)}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", style = MaterialTheme.typography.titleMedium)
                    Text("\$${String.format(Locale.getDefault(), "%.2f", viewModel.cartTotal)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppleButton(
                    text = "Cobrar \$${String.format(Locale.getDefault(), "%.2f", viewModel.cartTotal)}",
                    onClick = { showCheckout = true }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CartItemRow(item: CartItem, onQuantityChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.product.name, fontWeight = FontWeight.SemiBold)
            Text(text = "\$${String.format(Locale.getDefault(), "%.2f", item.product.price)} c/u", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            IconButton(onClick = { onQuantityChange(item.quantity - 1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(16.dp))
            }
            Text(text = item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
            IconButton(onClick = { onQuantityChange(item.quantity + 1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(16.dp))
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "\$${String.format(Locale.getDefault(), "%.2f", item.total)}",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutView(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onComplete: (Order) -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var expandedCustomerDropdown by remember { mutableStateOf(false) }

    var isDone by remember { mutableStateOf(false) }
    var processedOrder by remember { mutableStateOf<Order?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        if (!isDone) {
            TextButton(onClick = onBack) {
                Text("Atrás")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Método de Pago", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Total a cobrar: \$${String.format(Locale.getDefault(), "%.2f", viewModel.cartTotal)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            val methods = listOf(
                PaymentMethod.CASH to "Efectivo",
                PaymentMethod.CARD to "Tarjeta",
                PaymentMethod.TRANSFER to "Transferencia",
                PaymentMethod.CREDIT to "Fiado / Crédito"
            )

            methods.forEach { (method, name) ->
                val isSelected = selectedMethod == method
                Card(
                    onClick = {
                        selectedMethod = method
                        if (method != PaymentMethod.CREDIT) {
                            selectedCustomer = null
                        } else if (customers.isNotEmpty() && selectedCustomer == null) {
                            selectedCustomer = customers.first()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(16.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (selectedMethod == PaymentMethod.CREDIT) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Asignar Venta a Cliente", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedCustomerDropdown,
                    onExpandedChange = { expandedCustomerDropdown = !expandedCustomerDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCustomer?.name ?: "Seleccionar Cliente",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCustomerDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCustomerDropdown,
                        onDismissRequest = { expandedCustomerDropdown = false }
                    ) {
                        customers.forEach { customer ->
                            DropdownMenuItem(
                                text = { Text("${customer.name} (Deuda: \$${String.format(Locale.getDefault(), "%.2f", customer.debtBalance)})") },
                                onClick = {
                                    selectedCustomer = customer
                                    expandedCustomerDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val canConfirm = selectedMethod != null && (selectedMethod != PaymentMethod.CREDIT || selectedCustomer != null)

            AppleButton(
                text = "Confirmar Pago",
                enabled = canConfirm,
                onClick = {
                    val method = selectedMethod ?: PaymentMethod.CASH
                    processedOrder = viewModel.processCheckout(method, selectedCustomer?.id)
                    isDone = true
                },
                backgroundColor = if (canConfirm) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))
        } else {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF34C759)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = Color.White, style = MaterialTheme.typography.displayMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡Venta Completada!", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(24.dp))
                    AppleButton("Ver Ticket", onClick = { processedOrder?.let { onComplete(it) } })
                }
            }
        }
    }
}