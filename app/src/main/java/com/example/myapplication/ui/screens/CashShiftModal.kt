package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.local.CashShiftEntity
import com.example.myapplication.ui.components.AppleButton
import com.example.myapplication.ui.components.AppleCard
import com.example.myapplication.ui.theme.AppleBlue
import com.example.myapplication.ui.theme.AppleGreen
import com.example.myapplication.ui.theme.AppleRed
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashShiftModal(
    shiftViewModel: com.example.myapplication.viewmodel.ShiftViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onDismiss: () -> Unit
) {
    val activeShift by shiftViewModel.activeShift.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (activeShift == null) {
                OpenShiftView(
                    onConfirmOpen = { cashier, initialCash ->
                        shiftViewModel.openShift(cashier, initialCash)
                        onDismiss()
                    }
                )
            } else {
                CloseShiftView(
                    shift = activeShift!!,
                    onConfirmClose = { actualCash ->
                        shiftViewModel.closeShift(actualCash)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun OpenShiftView(
    onConfirmOpen: (cashierName: String, initialCash: Double) -> Unit
) {
    var cashierName by remember { mutableStateOf("Cajero 1") }
    var initialCashText by remember { mutableStateOf("500.0") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppleGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, tint = AppleGreen)
            }
            Column {
                Text("Apertura de Turno de Caja", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Ingresa el fondo inicial para abrir caja", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = AppleRed, style = MaterialTheme.typography.bodySmall)
        }

        OutlinedTextField(
            value = cashierName,
            onValueChange = { cashierName = it },
            label = { Text("Nombre del Cajero / Empleado") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = initialCashText,
            onValueChange = { initialCashText = it },
            label = { Text("Fondo Inicial en Caja ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        AppleButton(
            text = "Abrir Turno de Caja",
            backgroundColor = AppleGreen,
            onClick = {
                val initialCash = initialCashText.toDoubleOrNull()
                when {
                    cashierName.isBlank() -> errorMessage = "El nombre del cajero es obligatorio"
                    initialCash == null || initialCash < 0 -> errorMessage = "Ingresa un fondo inicial válido mayor o igual a 0"
                    else -> onConfirmOpen(cashierName.trim(), initialCash)
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CloseShiftView(
    shift: CashShiftEntity,
    onConfirmClose: (actualCash: Double) -> Unit
) {
    var actualCashText by remember { mutableStateOf(shift.expectedCash.toString()) }
    var errorMessage by remember { mutableStateOf("") }

    val actualCash = actualCashText.toDoubleOrNull() ?: 0.0
    val diff = actualCash - shift.expectedCash
    val formattedOpenTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(shift.openTimestamp)

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AppleRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AppleRed)
                }
                Column {
                    Text("Cierre Z y Arqueo de Caja", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Cajero: ${shift.cashierName} • Abierto: $formattedOpenTime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            AppleCard {
                Text("Desglose de Ingresos del Turno", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Fondo Inicial:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("\$${String.format(Locale.getDefault(), "%.2f", shift.initialCash)}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ventas Efectivo:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("+\$${String.format(Locale.getDefault(), "%.2f", shift.cashSales)}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ventas Tarjeta:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("\$${String.format(Locale.getDefault(), "%.2f", shift.cardSales)}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ventas Transferencia:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("\$${String.format(Locale.getDefault(), "%.2f", shift.transferSales)}")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ventas Crédito (Fiado):", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("\$${String.format(Locale.getDefault(), "%.2f", shift.creditSales)}")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Efectivo Esperado en Cajón:", fontWeight = FontWeight.Bold)
                    Text(
                        "\$${String.format(Locale.getDefault(), "%.2f", shift.expectedCash)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = AppleRed, style = MaterialTheme.typography.bodySmall)
            }

            OutlinedTextField(
                value = actualCashText,
                onValueChange = { actualCashText = it },
                label = { Text("Efectivo Contado Real ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            diff == 0.0 -> AppleBlue.copy(alpha = 0.15f)
                            diff > 0.0 -> AppleGreen.copy(alpha = 0.15f)
                            else -> AppleRed.copy(alpha = 0.15f)
                        }
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        diff == 0.0 -> "Caja Cuadrada"
                        diff > 0.0 -> "Sobrante de Caja"
                        else -> "Faltante de Caja"
                    },
                    fontWeight = FontWeight.Bold,
                    color = when {
                        diff == 0.0 -> AppleBlue
                        diff > 0.0 -> AppleGreen
                        else -> AppleRed
                    }
                )
                Text(
                    text = "\$${String.format(Locale.getDefault(), "%.2f", diff)}",
                    fontWeight = FontWeight.Bold,
                    color = when {
                        diff == 0.0 -> AppleBlue
                        diff > 0.0 -> AppleGreen
                        else -> AppleRed
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            AppleButton(
                text = "Generar Cierre Z y Cerrar Turno",
                backgroundColor = AppleRed,
                onClick = {
                    val count = actualCashText.toDoubleOrNull()
                    if (count == null || count < 0) {
                        errorMessage = "Ingresa el efectivo contado válido en caja"
                    } else {
                        onConfirmClose(count)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}