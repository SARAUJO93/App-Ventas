package com.novapos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val auth: FirebaseAuth get() = Firebase.auth

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(auth.currentUser?.email)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserEmail.value = firebaseAuth.currentUser?.email
        }
    }

    fun loginWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    _currentUserEmail.value = result.user?.email
                    _authState.value = AuthState.Success
                }
                .addOnFailureListener { _ ->
                    // Si el usuario no existe o las credenciales no son válidas en sign-in, intentamos registrarlo
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnSuccessListener { createResult ->
                            _currentUserEmail.value = createResult.user?.email
                            _authState.value = AuthState.Success
                        }
                        .addOnFailureListener { createError ->
                            val msg = createError.localizedMessage ?: "Error de autenticación."
                            _authState.value = AuthState.Error(msg)
                        }
                }
        }
    }

    fun loginWithGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    _currentUserEmail.value = result.user?.email
                    _authState.value = AuthState.Success
                }
                .addOnFailureListener { e ->
                    val msg = e.localizedMessage ?: "Error al autenticar con Google."
                    _authState.value = AuthState.Error(msg)
                }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        auth.signOut()
        _currentUserEmail.value = null
        _authState.value = AuthState.Idle
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Loading
            currentUser.delete()
                .addOnSuccessListener {
                    _currentUserEmail.value = null
                    _authState.value = AuthState.Idle
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    _authState.value = AuthState.Idle
                    val msg = e.localizedMessage ?: "Para eliminar tu cuenta, debes haber iniciado sesión recientemente."
                    onError(msg)
                }
        } else {
            onError("No hay usuario autenticado.")
        }
    }
}
