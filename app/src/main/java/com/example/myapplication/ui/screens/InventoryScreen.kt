package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Product
import com.example.myapplication.model.ProductModifier
import com.example.myapplication.model.ProductVariant
import com.example.myapplication.ui.components.AppleButton
import com.example.myapplication.ui.components.AppleCard
import com.example.myapplication.ui.theme.AppleRed
import com.example.myapplication.viewmodel.PosViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: PosViewModel) {
    val products by viewModel.products.collectAsState()
    var selectedCategory by remember { mutableStateOf("Todos") }
    var showOnlyLowStock by remember { mutableStateOf(false) }

    var showProductSheet by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("Todos", "Bebidas", "Alimentos", "Postres", "Panadería", "Otros")

    val filteredProducts = products.filter { product ->
        val matchesCategory = (selectedCategory == "Todos" || product.category == selectedCategory)
        val matchesLowStock = !showOnlyLowStock || product.stock < 20
        matchesCategory && matchesLowStock
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    productToEdit = null
                    showProductSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Producto", tint = Color.White)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Control de Inventario",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                FilterChip(
                    selected = showOnlyLowStock,
                    onClick = { showOnlyLowStock = !showOnlyLowStock },
                    label = { Text("Stock Bajo (<20)") },
                    leadingIcon = if (showOnlyLowStock) {
                        { Icon(Icons.Default.Warning, contentDescription = null, tint = AppleRed) }
                    } else null
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay productos en esta categoría",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 80.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        InventoryCard(
                            product = product,
                            onEdit = {
                                productToEdit = product
                                showProductSheet = true
                            },
                            onDelete = {
                                productToDelete = product
                            }
                        )
                    }
                }
            }
        }
    }

    if (showProductSheet) {
        AddEditProductSheet(
            product = productToEdit,
            categories = categories.filter { it != "Todos" },
            onDismiss = { showProductSheet = false },
            onSave = { name, price, category, stock, colorHex, barcode, variants, modifiers ->
                if (productToEdit == null) {
                    viewModel.addProduct(name, price, category, stock, colorHex, barcode, variants, modifiers)
                } else {
                    val updated = productToEdit!!.copy(
                        name = name,
                        price = price,
                        category = category,
                        stock = stock,
                        colorHex = colorHex,
                        barcode = barcode,
                        variants = variants,
                        modifiers = modifiers
                    )
                    viewModel.updateProduct(updated)
                }
                showProductSheet = false
            }
        )
    }

    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Estás seguro de que deseas eliminar \"${productToDelete?.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        productToDelete?.let { viewModel.deleteProduct(it.id) }
                        productToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AppleRed)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun InventoryCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AppleCard {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${product.category} • $${String.format("%.2f", product.price)}${if (product.variants.isNotEmpty()) " • ${product.variants.size} variantes" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val isLowStock = product.stock < 20
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "Stock: ${product.stock}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isLowStock) AppleRed else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                if (isLowStock) {
                    Text(
                        text = "Stock Bajo",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppleRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
fun AddEditProductSheet(
    product: Product?,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (name: String, price: Double, category: String, stock: Int, colorHex: Long, barcode: String?, variants: List<ProductVariant>, modifiers: List<ProductModifier>) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var priceText by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var stockText by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var barcodeText by remember { mutableStateOf(product?.barcode ?: "") }
    var selectedCategory by remember { mutableStateOf(product?.category ?: categories.firstOrNull() ?: "General") }

    val variantsList = remember { mutableStateListOf<ProductVariant>().apply { addAll(product?.variants ?: emptyList()) } }
    val modifiersList = remember { mutableStateListOf<ProductModifier>().apply { addAll(product?.modifiers ?: emptyList()) } }

    var newVariantName by remember { mutableStateOf("") }
    var newVariantPrice by remember { mutableStateOf("") }

    var newModifierName by remember { mutableStateOf("") }
    var newModifierPrice by remember { mutableStateOf("") }

    var showCameraScanner by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        0xFF8D6E63, // Cafe
        0xFFFFB74D, // Naranja / Panadería
        0xFF81C784, // Verde / Alimentos
        0xFF4FC3F7, // Azul / Bebidas
        0xFFF06292, // Rosa / Postres
        0xFFBA68C8  // Púrpura / Especiales
    )
    var selectedColorHex by remember { mutableStateOf(product?.colorHex ?: colorOptions.first()) }

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
                text = if (product == null) "Nuevo Producto" else "Editar Producto",
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
                label = { Text("Nombre del Producto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Precio Base ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = { Text("Stock Inicial") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = barcodeText,
                onValueChange = { barcodeText = it },
                label = { Text("Código de Barras / SKU") },
                trailingIcon = {
                    IconButton(onClick = { showCameraScanner = true }) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Escanear Cámara")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Categoría", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            // Sección Variantes (Tamaños)
            Text("Variantes / Tamaños (Opcional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newVariantName,
                    onValueChange = { newVariantName = it },
                    label = { Text("Ej. Grande") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newVariantPrice,
                    onValueChange = { newVariantPrice = it },
                    label = { Text("Precio ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = {
                    val p = newVariantPrice.toDoubleOrNull()
                    if (newVariantName.isNotBlank() && p != null) {
                        variantsList.add(ProductVariant(UUID.randomUUID().toString(), newVariantName.trim(), p))
                        newVariantName = ""
                        newVariantPrice = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Variante")
                }
            }

            if (variantsList.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(variantsList) { v ->
                        InputChip(
                            selected = true,
                            onClick = { variantsList.remove(v) },
                            label = { Text("${v.name} (\$${v.price})") },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remover") }
                        )
                    }
                }
            }

            // Sección Modificadores (Extras)
            Text("Modificadores / Extras (Opcional)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newModifierName,
                    onValueChange = { newModifierName = it },
                    label = { Text("Ej. Extra Queso") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newModifierPrice,
                    onValueChange = { newModifierPrice = it },
                    label = { Text("Costo (+$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = {
                    val p = newModifierPrice.toDoubleOrNull() ?: 0.0
                    if (newModifierName.isNotBlank()) {
                        modifiersList.add(ProductModifier(UUID.randomUUID().toString(), newModifierName.trim(), p))
                        newModifierName = ""
                        newModifierPrice = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Extra")
                }
            }

            if (modifiersList.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(modifiersList) { m ->
                        InputChip(
                            selected = true,
                            onClick = { modifiersList.remove(m) },
                            label = { Text("${m.name} (+\$${m.price})") },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remover") }
                        )
                    }
                }
            }

            Text("Color de Etiqueta", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                colorOptions.forEach { colorHex ->
                    val isSelected = selectedColorHex == colorHex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(colorHex))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorHex = colorHex }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppleButton(
                text = if (product == null) "Guardar Producto" else "Actualizar Producto",
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    val stock = stockText.toIntOrNull()

                    when {
                        name.isBlank() -> errorMessage = "El nombre del producto no puede estar vacío"
                        price == null || price <= 0 -> errorMessage = "Ingresa un precio válido mayor a 0"
                        stock == null || stock < 0 -> errorMessage = "Ingresa un stock válido mayor o igual a 0"
                        else -> {
                            onSave(name.trim(), price, selectedCategory, stock, selectedColorHex, barcodeText.trim().ifEmpty { null }, variantsList.toList(), modifiersList.toList())
                        }
                    }
                }
            )
        }
    }

    if (showCameraScanner) {
        BarcodeScannerModal(
            onDismiss = { showCameraScanner = false },
            onBarcodeScanned = { barcode ->
                barcodeText = barcode
                showCameraScanner = false
            }
        )
    }
}