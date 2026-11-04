package com.example.myapplication.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.BusinessSettings
import com.example.myapplication.model.Order
import com.example.myapplication.ui.components.AppleButton
import com.example.myapplication.viewmodel.PosViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketReceiptModal(
    order: Order,
    viewModel: PosViewModel? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val settingsState = viewModel?.businessSettings?.collectAsState()
    val settings = settingsState?.value ?: BusinessSettings()

    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(order.date)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Vista previa de Ticket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Simulación visual de ticket térmico
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF9F9F9))
                    .padding(16.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        Text(
                            text = settings.businessName.uppercase(),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "${settings.address}\nRFC: ${settings.taxId}\nTel: ${settings.phone}",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "--------------------------------",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fecha:", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.Black)
                            Text(formattedDate, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Orden #:", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.Black)
                            Text(order.id.takeLast(6), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.Black)
                        }
                        if (order.customerName != null) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Cliente:", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.Black)
                                Text(order.customerName, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.Black)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pago:", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.Black)
                            Text(order.paymentMethod?.name ?: "CONTANTO", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.Black)
                        }
                        Text(
                            text = "--------------------------------",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(order.items) { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.quantity}x ${item.product.name}", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                            Text("\$${String.format(Locale.getDefault(), "%.2f", item.total)}", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "--------------------------------",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SUBTOTAL", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                            Text("\$${String.format(Locale.getDefault(), "%.2f", order.subtotal)}", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                        }
                        if (order.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("DESCUENTO", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                                Text("-\$${String.format(Locale.getDefault(), "%.2f", order.discount)}", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IVA (16%)", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                            Text("\$${String.format(Locale.getDefault(), "%.2f", order.tax)}", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                        }
                        if (order.tip > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("PROPINA", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                                Text("+\$${String.format(Locale.getDefault(), "%.2f", order.tip)}", fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = Color.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("TOTAL", fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("\$${String.format(Locale.getDefault(), "%.2f", order.total)}", fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = settings.footerNote,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        val shareText = buildString {
                            appendLine("🧾 ${settings.businessName.uppercase()} - TICKET DE VENTA")
                            appendLine("Dirección: ${settings.address}")
                            appendLine("RFC: ${settings.taxId}")
                            appendLine("Tel: ${settings.phone}")
                            appendLine("Orden #: ${order.id.takeLast(6)}")
                            appendLine("Fecha: $formattedDate")
                            if (order.customerName != null) appendLine("Cliente: ${order.customerName}")
                            appendLine("--------------------------------")
                            order.items.forEach { item ->
                                appendLine("${item.quantity}x ${item.product.name} - \$${String.format(Locale.getDefault(), "%.2f", item.total)}")
                            }
                            appendLine("--------------------------------")
                            appendLine("Subtotal: \$${String.format(Locale.getDefault(), "%.2f", order.subtotal)}")
                            if (order.discount > 0) appendLine("Descuento: -\$${String.format(Locale.getDefault(), "%.2f", order.discount)}")
                            appendLine("IVA (16%): \$${String.format(Locale.getDefault(), "%.2f", order.tax)}")
                            if (order.tip > 0) appendLine("Propina: +\$${String.format(Locale.getDefault(), "%.2f", order.tip)}")
                            appendLine("TOTAL: \$${String.format(Locale.getDefault(), "%.2f", order.total)}")
                            appendLine("Pago: ${order.paymentMethod?.name ?: "EFECTIVO"}")
                            appendLine("\n${settings.footerNote}")
                        }
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir Recibo Nova POS"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Compartir")
                }

                AppleButton(
                    text = "Cerrar",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}