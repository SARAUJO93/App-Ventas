package com.novapos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.novapos.app.ui.screens.PosHomeScreen
import com.novapos.app.ui.screens.auth.BusinessNameScreen
import com.novapos.app.ui.screens.auth.EmailAuthScreen
import com.novapos.app.ui.screens.auth.WelcomeScreen
import com.novapos.app.ui.screens.onboarding.BusinessSectorScreen
import com.novapos.app.ui.screens.onboarding.ChallengesScreen
import com.novapos.app.ui.theme.MyApplicationTheme
import com.novapos.app.viewmodel.AuthViewModel
import com.novapos.app.viewmodel.OnboardingViewModel
import com.novapos.app.viewmodel.SettingsAndUpdateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RootNavigationGraph()
                }
            }
        }
    }
}

@Composable
fun RootNavigationGraph(
    settingsViewModel: SettingsAndUpdateViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentUserEmail by authViewModel.currentUserEmail.collectAsState()

    LaunchedEffect(currentUserEmail) {
        settingsViewModel.loadUserSettings(currentUserEmail)
    }

    val settings by settingsViewModel.businessSettings.collectAsState()

    // Determinamos el startDestination basado en el estado actual de la cuenta
    val startDest = when {
        currentUserEmail == null -> "welcome"
        settings.isFirstLaunch || settings.businessName.isBlank() -> "business_name"
        else -> "main_app"
    }

    NavHost(navController = navController, startDestination = startDest) {

        composable("welcome") {
            WelcomeScreen(
                authViewModel = authViewModel,
                onContinueWithEmail = { navController.navigate("email_auth") }
            )
            // Si el usuario ya está logueado pero entra a welcome, navegamos según su onboarding
            LaunchedEffect(currentUserEmail, settings.isFirstLaunch, settings.businessName) {
                if (currentUserEmail != null) {
                    val dest = if (settings.isFirstLaunch || settings.businessName.isBlank()) "business_name" else "main_app"
                    navController.navigate(dest) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable("email_auth") {
            EmailAuthScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    val dest = if (settings.isFirstLaunch || settings.businessName.isBlank()) "business_name" else "main_app"
                    navController.navigate(dest) {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("business_name") {
            BusinessNameScreen(
                settingsViewModel = settingsViewModel,
                onNext = { navController.navigate("onboarding_sector") }
            )
        }

        composable("onboarding_sector") {
            val selectedSector by onboardingViewModel.selectedSector.collectAsState()
            BusinessSectorScreen(
                selectedSector = selectedSector,
                onSectorSelected = { onboardingViewModel.selectSector(it) },
                onNext = { navController.navigate("onboarding_challenges") }
            )
        }

        composable("onboarding_challenges") {
            val selectedChallenges by onboardingViewModel.selectedChallenges.collectAsState()
            ChallengesScreen(
                selectedChallenges = selectedChallenges,
                onChallengeToggled = { onboardingViewModel.toggleChallenge(it) },
                onComplete = {
                    onboardingViewModel.completeOnboarding {
                        navController.navigate("main_app") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("main_app") {
            PosHomeScreen(
                onLogoutGlobal = {
                    authViewModel.logout()
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}