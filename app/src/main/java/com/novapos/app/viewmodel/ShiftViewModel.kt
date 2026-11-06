package com.novapos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novapos.app.data.local.CashShiftEntity
import com.novapos.app.data.local.EmployeeEntity
import com.novapos.app.data.local.TransactionEntity
import com.novapos.app.data.repository.PosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val repository: PosRepository
) : ViewModel() {

    val activeShift: StateFlow<CashShiftEntity?> = repository.getActiveShift()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions: StateFlow<List<TransactionEntity>> = repository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentEmployee = MutableStateFlow<EmployeeEntity?>(null)
    val currentEmployee: StateFlow<EmployeeEntity?> = _currentEmployee.asStateFlow()

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> = _failedAttempts.asStateFlow()

    private val _lockoutTimeRemaining = MutableStateFlow(0)
    val lockoutTimeRemaining: StateFlow<Int> = _lockoutTimeRemaining.asStateFlow()

    fun loginWithPin(pin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        if (_lockoutTimeRemaining.value > 0) {
            onError()
            return
        }

        viewModelScope.launch {
            val emp = repository.authenticatePin(pin)
            if (emp != null) {
                _failedAttempts.value = 0
                _currentEmployee.value = emp
                onSuccess()
            } else {
                val attempts = _failedAttempts.value + 1
                _failedAttempts.value = attempts
                if (attempts >= 5) {
                    startLockoutTimer()
                }
                onError()
            }
        }
    }

    private fun startLockoutTimer() {
        viewModelScope.launch {
            _lockoutTimeRemaining.value = 30
            while (_lockoutTimeRemaining.value > 0) {
                kotlinx.coroutines.delay(1000)
                _lockoutTimeRemaining.value -= 1
            }
            _failedAttempts.value = 0
        }
    }

    fun logoutEmployee() {
        _currentEmployee.value = null
    }

    fun createAdminPin(pin: String) {
        viewModelScope.launch {
            repository.createAdminEmployee("Administrador", pin)
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
