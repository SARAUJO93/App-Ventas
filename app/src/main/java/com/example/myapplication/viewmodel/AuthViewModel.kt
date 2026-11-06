package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            // Simulación de delay de red (Firebase Auth)
            delay(1500)

            if (email.contains("@") && pass.length >= 6) {
                _currentUserEmail.value = email
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error("Credenciales inválidas. Verifica tu correo y contraseña.")
            }
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            // Simulación de apertura de popup de Google y respuesta
            delay(1000)
            _currentUserEmail.value = "usuario.google@gmail.com"
            _authState.value = AuthState.Success
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        _currentUserEmail.value = null
        _authState.value = AuthState.Idle
    }
}