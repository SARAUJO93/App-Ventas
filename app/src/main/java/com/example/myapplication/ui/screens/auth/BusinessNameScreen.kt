package com.example.myapplication.ui.screens.auth

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
import com.example.myapplication.model.BusinessSettings
import com.example.myapplication.ui.components.AppleButton
import com.example.myapplication.ui.theme.AppleBlue
import com.example.myapplication.viewmodel.SettingsAndUpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessNameScreen(
    settingsViewModel: SettingsAndUpdateViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    var businessName by remember { mutableStateOf("") }

    // Obtenemos los settings actuales para poder actualizarlos
    val settings by settingsViewModel.businessSettings.collectAsState()

    Scaffold(
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                AppleButton(
                    text = "Siguiente",
                    enabled = businessName.isNotBlank(),
                    onClick = {
                        settingsViewModel.saveBusinessSettings(
                            settings.copy(businessName = businessName.trim())
                        )
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
            Spacer(modifier = Modifier.height(64.dp))

            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = AppleBlue
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¿Cómo le llamamos a tu negocio?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )

            Spacer(modifier = Modifier.height(48.dp))

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
        }
    }
}