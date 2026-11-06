package com.novapos.app.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.novapos.app.ui.theme.AppleBlue
import com.novapos.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    authViewModel: AuthViewModel,
    onContinueWithEmail: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = CredentialManager.create(context)
    val authState by authViewModel.authState.collectAsState()

    val webClientId = remember(context) {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            context.getString(resId)
        } else {
            "636959307320-fefbnija8d8qmc9ak1qg151pe501qsc0.apps.googleusercontent.com"
        }
    }

    var showDeveloperErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        when (authState) {
            is com.novapos.app.viewmodel.AuthState.Error -> {
                val msg = (authState as com.novapos.app.viewmodel.AuthState.Error).message
                Toast.makeText(context, "Error de inicio de sesión: $msg", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                authViewModel.loginWithGoogleIdToken(idToken)
            } else {
                Toast.makeText(context, "No se pudo obtener el token de Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            if (e.statusCode == 10) {
                showDeveloperErrorDialog = true
            } else if (e.statusCode != 12501) {
                Toast.makeText(context, "Error Google Sign-In (${e.statusCode}): ${e.localizedMessage ?: "Credenciales no válidas"}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun launchLegacyGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    fun launchGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    authViewModel.loginWithGoogleIdToken(googleIdTokenCredential.idToken)
                } else {
                    launchLegacyGoogleSignIn()
                }
            } catch (e: Exception) {
                launchLegacyGoogleSignIn()
            }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF3F7FA),
            Color(0xFFFFFFFF)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "NOVA",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Black,
            color = AppleBlue,
            letterSpacing = 2.sp
        )
        Text(
            text = "Punto de venta",
            style = MaterialTheme.typography.titleMedium,
            color = AppleBlue,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Simplifica tu negocio",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        val features = listOf("Gastos", "Inventario", "Empleados", "Lealtad", "Analítica")

        features.forEachIndexed { index, feature ->
            val alpha = 1f - (Math.abs(index - 2) * 0.3f)
            Text(
                text = feature,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50).copy(alpha = alpha.coerceIn(0.2f, 1f)),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón Google
        Button(
            onClick = { launchGoogleSignIn() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppleBlue)
        ) {
            Text("G", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
            Text("Continuar con Google", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(">", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Email
        OutlinedButton(
            onClick = onContinueWithEmail,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(1.dp, Color.Transparent),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF2C3E50)
            )
        ) {
            Text("✉", fontSize = 20.sp, modifier = Modifier.padding(end = 16.dp))
            Text("Continuar con Email", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(">", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Al continuar, aceptas nuestras política de privacidad y términos de uso",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    if (showDeveloperErrorDialog) {
        AlertDialog(
            onDismissRequest = { showDeveloperErrorDialog = false },
            title = { Text("Configuración de Google Auth requerida") },
            text = {
                Text(
                    "Google ha rechazado la solicitud (Error 10) porque el paquete 'com.novapos.app' debe agregarse a tu consola de Firebase con tu huella SHA-1.\n\n" +
                    "Puedes usar 'Continuar con Email' que funciona 100% de inmediato sin este requisito."
                )
            },
            confirmButton = {
                TextButton(onClick = { showDeveloperErrorDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}