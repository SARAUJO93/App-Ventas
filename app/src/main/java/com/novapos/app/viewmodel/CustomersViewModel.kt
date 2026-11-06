package com.novapos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novapos.app.data.repository.PosRepository
import com.novapos.app.model.Customer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CustomersViewModel @Inject constructor(
    private val repository: PosRepository
) : ViewModel() {

    val customers: StateFlow<List<Customer>> = repository.getCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomer(name: String, phone: String, email: String, initialDebt: Double) {
        viewModelScope.launch {
            val customer = Customer(
                id = UUID.randomUUID().toString(),
                name = name,
                phone = phone,
                email = email,
                debtBalance = initialDebt
            )
            repository.addCustomer(customer)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customerId: String) {
        viewModelScope.launch {
            repository.deleteCustomer(customerId)
        }
    }

    fun recordCustomerPayment(customerId: String, paymentAmount: Double) {
        viewModelScope.launch {
            repository.recordCustomerPayment(customerId, paymentAmount)
        }
    }
}
