package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Customer
import com.example.myapplication.ui.components.AppleButton
import com.example.myapplication.ui.components.AppleCard
import com.example.myapplication.ui.theme.AppleGreen
import com.example.myapplication.ui.theme.AppleRed
import com.example.myapplication.viewmodel.PosViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(viewModel: PosViewModel) {
    val customers by viewModel.customers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    var showCustomerSheet by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    var customerToAbonar by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customers.filter { customer ->
        searchQuery.isBlank() ||
                customer.name.contains(searchQuery, ignoreCase = true) ||
                customer.phone.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    customerToEdit = null
                    showCustomerSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo Cliente", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Directorio de Clientes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar cliente por nombre o teléfono...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No hay clientes registrados" else "Sin resultados de búsqueda",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onEdit = {
                                customerToEdit = customer
                                showCustomerSheet = true
                            },
                            onDelete = {
                                customerToDelete = customer
                            },
                            onAbonar = {
                                customerToAbonar = customer
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCustomerSheet) {
        AddEditCustomerSheet(
            customer = customerToEdit,
            onDismiss = { showCustomerSheet = false },
            onSave = { name, phone, email, initialDebt ->
                if (customerToEdit == null) {
                    viewModel.addCustomer(name, phone, email, initialDebt)
                } else {
                    val updated = customerToEdit!!.copy(
                        name = name,
                        phone = phone,
                        email = email,
                        debtBalance = initialDebt
                    )
                    viewModel.updateCustomer(updated)
                }
                showCustomerSheet = false
            }
        )
    }

    if (customerToAbonar != null) {
        RecordPaymentSheet(
            customer = customerToAbonar!!,
            onDismiss = { customerToAbonar = null },
            onConfirmPayment = { amount ->
                viewModel.recordCustomerPayment(customerToAbonar!!.id, amount)
                customerToAbonar = null
            }
        )
    }

    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Eliminar Cliente") },
            text = { Text("¿Deseas eliminar a \"${customerToDelete?.name}\"? Se eliminará su registro de clientes.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        customerToDelete?.let { viewModel.deleteCustomer(it.id) }
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppleRed)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun CustomerCard(
    customer: Customer,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAbonar: () -> Unit
) {
    AppleCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.firstOrNull()?.toString()?.uppercase() ?: "C",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (customer.phone.isNotEmpty()) {
                    Text(
                        text = customer.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (customer.debtBalance > 0) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(text = "Deuda", style = MaterialTheme.typography.labelSmall, color = AppleRed)
                    Text(
                        text = "\$${String.format(Locale.getDefault(), "%.2f", customer.debtBalance)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppleRed,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = onAbonar,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Abonar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            } else {
                Text(
                    text = "Al día",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppleGreen,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = AppleRed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerSheet(
    customer: Customer?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, initialDebt: Double) -> Unit
) {
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var debtText by remember { mutableStateOf(customer?.debtBalance?.toString() ?: "0.0") }
    var errorMessage by remember { mutableStateOf("") }

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
                text = if (customer == null) "Nuevo Cliente" else "Editar Cliente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = AppleRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Teléfono de Contacto") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico (Opcional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = debtText,
                onValueChange = { debtText = it },
                label = { Text("Saldo Deudor / Deuda Inicial ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppleButton(
                text = if (customer == null) "Guardar Cliente" else "Actualizar Cliente",
                onClick = {
                    val debt = debtText.toDoubleOrNull()
                    when {
                        name.isBlank() -> errorMessage = "El nombre del cliente es obligatorio"
                        debt == null || debt < 0 -> errorMessage = "Ingresa un saldo deudor válido mayor o igual a 0"
                        else -> {
                            onSave(name.trim(), phone.trim(), email.trim(), debt)
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentSheet(
    customer: Customer,
    onDismiss: () -> Unit,
    onConfirmPayment: (amount: Double) -> Unit
) {
    var paymentText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

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
                text = "Registrar Abono a Deuda",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Cliente: ${customer.name}\nDeuda Actual: \$${String.format(Locale.getDefault(), "%.2f", customer.debtBalance)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = AppleRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = paymentText,
                onValueChange = { paymentText = it },
                label = { Text("Monto a Abonar ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { paymentText = (customer.debtBalance / 2).toString() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("50%")
                }
                OutlinedButton(
                    onClick = { paymentText = customer.debtBalance.toString() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pagar Todo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppleButton(
                text = "Confirmar Abono",
                backgroundColor = AppleGreen,
                onClick = {
                    val amount = paymentText.toDoubleOrNull()
                    when {
                        amount == null || amount <= 0 -> errorMessage = "Ingresa un monto de abono válido mayor a 0"
                        amount > customer.debtBalance -> errorMessage = "El abono no puede superar la deuda actual (\$${String.format(Locale.getDefault(), "%.2f", customer.debtBalance)})"
                        else -> {
                            onConfirmPayment(amount)
                        }
                    }
                }
            )
        }
    }
}