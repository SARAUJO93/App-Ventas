package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.TableItemEntity
import com.example.myapplication.data.repository.PosRepository
import com.example.myapplication.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
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

    val orders: StateFlow<List<Order>> = repository.getOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems = _cartItems.asStateFlow()

    private val _tipPercent = MutableStateFlow(0.0)
    val tipPercent = _tipPercent.asStateFlow()

    private val _discountPercent = MutableStateFlow(0.0)
    val discountPercent = _discountPercent.asStateFlow()

    private var activeCheckoutTableId: String? = null

    val cartSubtotal: Double
        get() = _cartItems.value.sumOf { it.total }

    val cartDiscount: Double
        get() = cartSubtotal * (_discountPercent.value / 100.0)

    val cartTaxableAmount: Double
        get() = (cartSubtotal - cartDiscount).coerceAtLeast(0.0)

    val cartTax: Double
        get() = cartTaxableAmount * 0.16

    val cartTip: Double
        get() = cartTaxableAmount * (_tipPercent.value / 100.0)

    val cartTotal: Double
        get() = cartTaxableAmount + cartTax + cartTip

    fun setTipPercent(percent: Double) {
        _tipPercent.value = percent
    }

    fun setDiscountPercent(percent: Double) {
        _discountPercent.value = percent
    }

    fun getTableItems(tableId: String): Flow<List<TableItemEntity>> {
        return repository.getTableItems(tableId)
    }

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
        _tipPercent.value = 0.0
        _discountPercent.value = 0.0
        activeCheckoutTableId = null
    }

    fun addProduct(name: String, price: Double, category: String, stock: Int, colorHex: Long) {
        viewModelScope.launch {
            val newProduct = Product(
                id = UUID.randomUUID().toString(),
                name = name,
                price = price,
                category = category,
                stock = stock,
                colorHex = colorHex
            )
            repository.addProduct(newProduct)
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
        }
    }

    fun updateTableStatus(table: Table, newStatus: TableStatus) {
        viewModelScope.launch {
            repository.updateTable(table.copy(status = newStatus))
        }
    }

    fun addItemToTable(tableId: String, product: Product) {
        viewModelScope.launch {
            repository.addItemToTable(tableId, product)
        }
    }

    fun removeItemFromTable(tableId: String, productId: String) {
        viewModelScope.launch {
            repository.removeItemFromTable(tableId, productId)
        }
    }

    fun clearTableOrder(tableId: String) {
        viewModelScope.launch {
            repository.clearTableOrder(tableId)
        }
    }

    fun transferTableOrderToCart(tableId: String, onTransferred: () -> Unit) {
        viewModelScope.launch {
            val items = repository.getTableItems(tableId).first()
            if (items.isNotEmpty()) {
                val converted = items.map { item ->
                    CartItem(
                        product = Product(
                            id = item.productId,
                            name = item.productName,
                            price = item.unitPrice,
                            category = "Mesa",
                            stock = 999,
                            colorHex = item.colorHex
                        ),
                        quantity = item.quantity
                    )
                }
                _cartItems.value = converted
                activeCheckoutTableId = tableId
                onTransferred()
            }
        }
    }

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

    fun processCheckout(method: PaymentMethod, customerId: String? = null): Order {
        val currentItems = _cartItems.value.toList()
        val tableIdToClear = activeCheckoutTableId
        val totalToCharge = cartTotal
        val customerObj = customers.value.find { it.id == customerId }

        val newOrder = Order(
            id = System.currentTimeMillis().toString(),
            items = currentItems,
            subtotal = cartSubtotal,
            discount = cartDiscount,
            tax = cartTax,
            tip = cartTip,
            total = totalToCharge,
            status = OrderStatus.COMPLETED,
            paymentMethod = method,
            customerId = customerId,
            customerName = customerObj?.name
        )
        viewModelScope.launch {
            repository.saveOrder(newOrder)
            repository.deductStock(currentItems)
            if (method == PaymentMethod.CREDIT && customerId != null) {
                repository.addCustomerDebt(customerId, totalToCharge)
            }
            if (tableIdToClear != null) {
                repository.clearTableOrder(tableIdToClear)
            }
        }
        clearCart()
        return newOrder
    }
}