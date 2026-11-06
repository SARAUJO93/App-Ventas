package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.CashShiftEntity
import com.example.myapplication.data.local.EmployeeEntity
import com.example.myapplication.data.local.TransactionEntity
import com.example.myapplication.data.repository.PosRepository
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

    fun loginWithPin(pin: String, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val emp = repository.authenticatePin(pin)
            if (emp != null) {
                _currentEmployee.value = emp
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun logoutEmployee() {
        _currentEmployee.value = null
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