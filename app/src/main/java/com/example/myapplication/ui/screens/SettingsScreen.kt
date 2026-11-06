package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.model.BusinessSettings
import com.example.myapplication.ui.components.AppleButton
import com.example.myapplication.ui.components.AppleCard
import com.example.myapplication.ui.components.AppleSegmentedControl
import com.example.myapplication.ui.theme.AppleGreen
import com.example.myapplication.viewmodel.SettingsAndUpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsAndUpdateViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onLogoutGlobal: () -> Unit
) {
    val currentSettings by settingsViewModel.businessSettings.collectAsState()

    var businessName by remember { mutableStateOf(currentSettings.businessName) }
    var address by remember { mutableStateOf(currentSettings.address) }
    var taxId by remember { mutableStateOf(currentSettings.taxId) }
    var phone by remember { mutableStateOf(currentSettings.phone) }
    var footerNote by remember { mutableStateOf(currentSettings.footerNote) }
    var autoPrintReceipt by remember { mutableStateOf(currentSettings.autoPrintReceipt) }
    var paperSizeIndex by remember { mutableStateOf(if (currentSettings.paperSize == "80mm") 1 else 0) }
    var printerName by remember { mutableStateOf(currentSettings.printerName) }

    var isSavedSuccess by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ajustes del Negocio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isSavedSuccess) {
                    Text(
                        text = "¡Guardado!",
                        color = AppleGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                // Sección 1: Datos del Negocio
                item {
                    AppleCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Información Comercial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it; isSavedSuccess = false },
                            label = { Text("Nombre del Negocio") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it; isSavedSuccess = false },
                            label = { Text("Dirección / Sucursal") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            singleLine = true
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = taxId,
                                onValueChange = { taxId = it; isSavedSuccess = false },
                                label = { Text("RFC / Tax ID") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it; isSavedSuccess = false },
                                label = { Text("Teléfono") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = footerNote,
                            onValueChange = { footerNote = it; isSavedSuccess = false },
                            label = { Text("Mensaje al Pie del Ticket") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            singleLine = true
                        )
                    }
                }

                // Sección 2: Configuración de Impresora
                item {
                    AppleCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Impresión de Tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-imprimir Recibo", fontWeight = FontWeight.SemiBold)
                                Text("Abrir ticket automáticamente al cobrar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = autoPrintReceipt,
                                onCheckedChange = { autoPrintReceipt = it; isSavedSuccess = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ancho de Papel Térmico", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        AppleSegmentedControl(
                            items = listOf("58 mm (Estándar)", "80 mm (Ancho)"),
                            selectedIndex = paperSizeIndex,
                            onItemSelected = { paperSizeIndex = it; isSavedSuccess = false }
                        )

                        OutlinedTextField(
                            value = printerName,
                            onValueChange = { printerName = it; isSavedSuccess = false },
                            label = { Text("Nombre / Dirección de Impresora Bluetooth") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            singleLine = true
                        )
                    }
                }

                // Sección 3: Sistema y Actualizaciones OTA
                item {
                    AppleCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Acerca de Nova POS", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        val versionName = com.example.myapplication.BuildConfig.VERSION_NAME
                        val versionCode = com.example.myapplication.BuildConfig.VERSION_CODE

                        Text("Versión instalada: v$versionName (Build $versionCode)", style = MaterialTheme.typography.bodyMedium)
                        Text("Base de Datos Room v7 • Hilt DI", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                settingsViewModel.checkForUpdates(versionName)
                                showUpdateDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscar Actualizaciones OTA")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppleButton(
                text = "Guardar Ajustes",
                onClick = {
                    val updatedSettings = BusinessSettings(
                        businessName = businessName.trim().ifEmpty { "NOVA POS CAFE" },
                        address = address.trim(),
                        taxId = taxId.trim(),
                        phone = phone.trim(),
                        footerNote = footerNote.trim().ifEmpty { "¡GRACIAS POR SU COMPRA!" },
                        autoPrintReceipt = autoPrintReceipt,
                        paperSize = if (paperSizeIndex == 1) "80mm" else "58mm",
                        printerName = printerName.trim()
                    )
                    settingsViewModel.saveBusinessSettings(updatedSettings)
                    isSavedSuccess = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogoutGlobal,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar Sesión Global (Cambiar Cuenta)")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showUpdateDialog) {
        UpdateDialog(
            onDismiss = { showUpdateDialog = false }
        )
    }
}