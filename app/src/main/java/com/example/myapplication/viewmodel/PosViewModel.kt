package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.PosRepository
import com.example.myapplication.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PosViewModel @Inject constructor(
    private val repository: PosRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tables: StateFlow<List<Table>> = repository.getTables()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.getCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    val cartSubtotal: Double
        get() = _cartItems.value.sumOf { it.total }

    val cartTax: Double
        get() = cartSubtotal * 0.16

    val cartTotal: Double
        get() = cartSubtotal + cartTax

    fun addToCart(product: Product) {
        _cartItems.update { current ->
            val existing = current.find { it.product.id == product.id }
            if (existing != null) {
                current.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it }
            } else {
                current + CartItem(product, 1)
            }
        }
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            _cartItems.update { current -> current.filter { it.product.id != productId } }
        } else {
            _cartItems.update { current ->
                current.map { if (it.product.id == productId) it.copy(quantity = newQuantity) else it }
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun processCheckout(method: PaymentMethod): Order {
        val newOrder = Order(
            id = System.currentTimeMillis().toString(),
            items = _cartItems.value.toList(),
            subtotal = cartSubtotal,
            tax = cartTax,
            total = cartTotal,
            status = OrderStatus.COMPLETED,
            paymentMethod = method
        )
        clearCart()
        return newOrder
    }
}