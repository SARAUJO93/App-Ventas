package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.repository.UpdateState
import com.example.myapplication.ui.components.AppleButton
import com.example.myapplication.ui.theme.AppleBlue
import com.example.myapplication.ui.theme.AppleGreen
import com.example.myapplication.ui.theme.AppleRed
import com.example.myapplication.viewmodel.SettingsAndUpdateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(
    settingsViewModel: SettingsAndUpdateViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val updateState by settingsViewModel.updateState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = {
            settingsViewModel.resetUpdateState()
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(AppleBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = AppleBlue,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Actualizaciones de Nova POS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            when (val state = updateState) {
                is UpdateState.Idle -> {
                    Text("Presiona para buscar actualizaciones en GitHub.")
                    AppleButton("Buscar Ahora", onClick = { settingsViewModel.checkForUpdates() })
                }
                is UpdateState.Checking -> {
                    CircularProgressIndicator()
                    Text("Consultando GitHub Releases...", style = MaterialTheme.typography.bodyMedium)
                }
                is UpdateState.NoUpdateAvailable -> {
                    Text(
                        text = "¡Tienes la versión más reciente de Nova POS! (v1.0.0)",
                        color = AppleGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    AppleButton("Aceptar", onClick = {
                        settingsViewModel.resetUpdateState()
                        onDismiss()
                    })
                }
                is UpdateState.UpdateAvailable -> {
                    Text(
                        text = "🚀 ¡Nueva versión disponible: v${state.info.latestVersion}!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppleBlue
                    )
                    Text(
                        text = "Novedades:\n${state.info.releaseNotes}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppleButton(
                        text = "Descargar e Instalar",
                        onClick = { settingsViewModel.startUpdateDownload(state.info) }
                    )
                }
                is UpdateState.Downloading -> {
                    val percent = (state.progress * 100).toInt()
                    Text(text = "Descargando actualización: $percent%", fontWeight = FontWeight.SemiBold)
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                }
                is UpdateState.ReadyToInstall -> {
                    Text(
                        text = "¡Descarga completada! Presiona para iniciar la instalación.",
                        color = AppleGreen,
                        fontWeight = FontWeight.Bold
                    )
                    AppleButton(
                        text = "Instalar Ahora",
                        backgroundColor = AppleGreen,
                        onClick = {
                            settingsViewModel.installApk(context, state.apkFile)
                            settingsViewModel.resetUpdateState()
                            onDismiss()
                        }
                    )
                }
                is UpdateState.Error -> {
                    Text(text = state.message, color = AppleRed)
                    AppleButton("Reintentar", onClick = { settingsViewModel.checkForUpdates() })
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}