package com.novapos.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novapos.app.data.local.CashShiftEntity
import com.novapos.app.data.local.EmployeeEntity
import com.novapos.app.data.local.TransactionEntity
import com.novapos.app.data.repository.PosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PosRepository
) : ViewModel() {

    private val prefs = context.getSharedPreferences("nova_pos_pin_security", Context.MODE_PRIVATE)

    val activeShift: StateFlow<CashShiftEntity?> = repository.getActiveShift()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<TransactionEntity>> = repository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentEmployee = MutableStateFlow<EmployeeEntity?>(null)
    val currentEmployee: StateFlow<EmployeeEntity?> = _currentEmployee.asStateFlow()

    private val _failedAttempts = MutableStateFlow(prefs.getInt("failedAttempts", 0))
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _lockoutTimeRemaining = MutableStateFlow(0)
    val lockoutTimeRemaining: StateFlow<Int> = _lockoutTimeRemaining.asStateFlow()

    init {
        checkLockoutStatus()
    }

    private fun checkLockoutStatus() {
        val lockoutUntil = prefs.getLong("lockoutUntilTimestamp", 0L)
        val now = System.currentTimeMillis()
        if (lockoutUntil > now) {
            val remainingSecs = ((lockoutUntil - now) / 1000).toInt()
            if (remainingSecs > 0) {
                _lockoutTimeRemaining.value = remainingSecs
                startLockoutCountdownFrom(remainingSecs)
            }
        }
    }

    fun loginWithPin(pin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        if (_lockoutTimeRemaining.value > 0) {
            onError()
            return
        }

        viewModelScope.launch {
            val emp = repository.authenticatePin(pin)
            if (emp != null) {
                _failedAttempts.value = 0
                prefs.edit().putInt("failedAttempts", 0).putLong("lockoutUntilTimestamp", 0L).apply()
                _currentEmployee.value = emp
                onSuccess()
            } else {
                val attempts = _failedAttempts.value + 1
                _failedAttempts.value = attempts
                prefs.edit().putInt("failedAttempts", attempts).apply()
                if (attempts >= 5) {
                    val lockoutUntil = System.currentTimeMillis() + 30000L
                    prefs.edit().putLong("lockoutUntilTimestamp", lockoutUntil).apply()
                    startLockoutCountdownFrom(30)
                }
                onError()
            }
        }
    }

    private fun startLockoutCountdownFrom(seconds: Int) {
        viewModelScope.launch {
            _lockoutTimeRemaining.value = seconds
            while (_lockoutTimeRemaining.value > 0) {
                kotlinx.coroutines.delay(1000)
                _lockoutTimeRemaining.value -= 1
            }
            _failedAttempts.value = 0
            prefs.edit().putInt("failedAttempts", 0).putLong("lockoutUntilTimestamp", 0L).apply()
        }
    }

    fun logoutEmployee() {
        _currentEmployee.value = null
    }

    fun createAdminPin(pin: String) {
        viewModelScope.launch {
            repository.createAdminEmployee("Administrador", pin)
            val emp = repository.authenticatePin(pin)
            if (emp != null) {
                _currentEmployee.value = emp
            }
        }
    }

    fun verifyAdminPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val emp = repository.authenticatePin(pin)
            onResult(emp?.role == "ADMIN")
        }
    }

    fun openShift(cashierName: String, initialCash: Double) {
        viewModelScope.launch {
            repository.openShift(cashierName, initialCash)
        }
    }

    fun closeShift(actualCash: Double) {
        viewModelScope.launch {
            val current = activeShift.value
            if (current != null) {
                repository.closeShift(current.id, actualCash)
            }
        }
    }

    fun addTransaction(type: String, amount: Double, category: String, note: String) {
        viewModelScope.launch {
            repository.addTransaction(type, amount, category, note)
        }
    }
}