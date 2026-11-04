package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PosViewModel : ViewModel() {

    // Mock Data
    private val mockProducts = listOf(
        Product("1", "Café Americano", 35.0, "Bebidas", 50, 0xFF8D6E63),
        Product("2", "Capuccino", 45.0, "Bebidas", 40, 0xFF8D6E63),
        Product("3", "Croissant", 30.0, "Panadería", 20, 0xFFFFB74D),
        Product("4", "Sándwich de Pavo", 75.0, "Alimentos", 15, 0xFF81C784),
        Product("5", "Agua Mineral", 25.0, "Bebidas", 100, 0xFF4FC3F7),
        Product("6", "Cheesecake", 55.0, "Postres", 10, 0xFFF06292)
    )

    private val mockTables = listOf(
        Table("1", "Mesa 1", TableStatus.OCCUPIED, 125.0, 45),
        Table("2", "Mesa 2", TableStatus.AVAILABLE),
        Table("3", "Mesa 3", TableStatus.WAITING_BILL, 350.0, 120),
        Table("4", "Mesa 4", TableStatus.AVAILABLE),
        Table("5", "Barra 1", TableStatus.OCCUPIED, 45.0, 15),
    )

    private val mockCustomers = listOf(
        Customer("1", "Juan Pérez", "555-1234", debtBalance = 150.0),
        Customer("2", "María García", "555-5678", debtBalance = 0.0),
        Customer("3", "Carlos López", "555-8765", debtBalance = 450.0)
    )

    // States
    private val _products = MutableStateFlow(mockProducts)
    val products = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _tables = MutableStateFlow(mockTables)
    val tables = _tables.asStateFlow()

    private val _customers = MutableStateFlow(mockCustomers)
    val customers = _customers.asStateFlow()

    // Totals
    val cartSubtotal: Double
        get() = _cartItems.value.sumOf { it.total }

    val cartTax: Double
        get() = cartSubtotal * 0.16 // 16% IVA

    val cartTotal: Double
        get() = cartSubtotal + cartTax

    // Cart Actions
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