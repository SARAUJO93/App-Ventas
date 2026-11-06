package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.PosHomeScreen
import com.example.myapplication.ui.screens.auth.BusinessNameScreen
import com.example.myapplication.ui.screens.auth.EmailAuthScreen
import com.example.myapplication.ui.screens.auth.WelcomeScreen
import com.example.myapplication.ui.screens.onboarding.BusinessSectorScreen
import com.example.myapplication.ui.screens.onboarding.ChallengesScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodel.AuthViewModel
import com.example.myapplication.viewmodel.OnboardingViewModel
import com.example.myapplication.viewmodel.SettingsAndUpdateViewModel
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
    val settings by settingsViewModel.businessSettings.collectAsState()
    val currentUserEmail by authViewModel.currentUserEmail.collectAsState()

    // Determinamos el startDestination basado en el estado
    val startDest = when {
        currentUserEmail == null -> "welcome"
        settings.isFirstLaunch -> "business_name"
        else -> "main_app"
    }

    NavHost(navController = navController, startDestination = startDest) {
        
        composable("welcome") {
            WelcomeScreen(
                onContinueWithGoogle = { authViewModel.loginWithGoogle() },
                onContinueWithEmail = { navController.navigate("email_auth") }
            )
        }

        composable("email_auth") {
            EmailAuthScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigate("business_name") {
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
                            popUpTo("business_name") { inclusive = true }
                            popUpTo("welcome") { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("main_app") {
            PosHomeScreen()
        }
    }
}