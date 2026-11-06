package com.novapos.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.novapos.app.model.BusinessSettings
import com.novapos.app.ui.components.AppleButton
import com.novapos.app.ui.theme.AppleBlue
import com.novapos.app.viewmodel.SettingsAndUpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessNameScreen(
    settingsViewModel: SettingsAndUpdateViewModel = hiltViewModel(),
    shiftViewModel: com.novapos.app.viewmodel.ShiftViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    var businessName by remember { mutableStateOf("") }
    var adminPin by remember { mutableStateOf("") }

    val settings by settingsViewModel.businessSettings.collectAsState()

    val canProceed = businessName.isNotBlank() && adminPin.length == 4

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                AppleButton(
                    text = "Siguiente",
                    enabled = canProceed,
                    onClick = {
                        settingsViewModel.saveBusinessSettings(
                            settings.copy(businessName = businessName.trim())
                        )
                        shiftViewModel.createAdminPin(adminPin)
                        onNext()
                    }
                )
            }
        },
        containerColor = Color(0xFFF3F7FA)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AppleBlue
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Configura tu negocio y clave",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Nombre de tu negocio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF2C3E50),
                    unfocusedTextColor = Color(0xFF2C3E50),
                    focusedLabelColor = AppleBlue,
                    unfocusedLabelColor = Color(0xFF718096),
                    focusedBorderColor = AppleBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = adminPin,
                onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) adminPin = it },
                label = { Text("Crea tu PIN de Administrador (4 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = Color(0xFF2C3E50),
                    unfocusedTextColor = Color(0xFF2C3E50),
                    focusedLabelColor = AppleBlue,
                    unfocusedLabelColor = Color(0xFF718096),
                    focusedBorderColor = AppleBlue,
                    unfocusedBorderColor = Color(0xFFCBD5E0)
                )
            )
        }
    }
}
