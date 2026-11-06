package com.novapos.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novapos.app.model.Product
import com.novapos.app.model.ProductModifier
import com.novapos.app.model.ProductVariant
import com.novapos.app.ui.components.AppleButton
import com.novapos.app.ui.components.AppleCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectVariantModal(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (selectedVariant: ProductVariant?, selectedModifiers: List<ProductModifier>) -> Unit
) {
    var selectedVariant by remember { mutableStateOf(product.variants.firstOrNull()) }
    val selectedModifiers = remember { mutableStateListOf<ProductModifier>() }

    val basePrice = selectedVariant?.price ?: product.price
    val modifiersTotal = selectedModifiers.sumOf { it.price }
    val itemTotal = basePrice + modifiersTotal

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(product.colorHex))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (product.variants.isNotEmpty()) {
                Text("Selecciona el Tamaño / Variante", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(product.variants) { variant ->
                        FilterChip(
                            selected = selectedVariant?.id == variant.id,
                            onClick = { selectedVariant = variant },
                            label = { Text("${variant.name} (\$${String.format(Locale.getDefault(), "%.2f", variant.price)})") },
                            leadingIcon = if (selectedVariant?.id == variant.id) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                    }
                }
            }

            if (product.modifiers.isNotEmpty()) {
                Text("Adicionales / Modificadores", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(product.modifiers) { modifier ->
                        val isSelected = selectedModifiers.any { it.id == modifier.id }
                        AppleCard(
                            onClick = {
                                if (isSelected) {
                                    selectedModifiers.removeAll { it.id == modifier.id }
                                } else {
                                    selectedModifiers.add(modifier)
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = modifier.name, fontWeight = FontWeight.SemiBold)
                                }
                                Text(
                                    text = if (modifier.price > 0) "+\$${String.format(Locale.getDefault(), "%.2f", modifier.price)}" else "Gratis",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppleButton(
                text = "Agregar al Carrito • \$${String.format(Locale.getDefault(), "%.2f", itemTotal)}",
                onClick = {
                    onConfirm(selectedVariant, selectedModifiers.toList())
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
