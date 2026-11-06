package com.novapos.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.novapos.app.data.repository.PosRepository
import com.novapos.app.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val selectedServiceType: ServiceType = ServiceType.DINE_IN,
    val selectedCustomer: Customer? = null,
    val tipPercent: Double = 0.0,
    val discountPercent: Double = 0.0,
    val activeTableId: String? = null
) {
    val subtotal: Double get() = items.sumOf { it.total }
    val discount: Double get() = subtotal * (discountPercent / 100.0)
    val taxableAmount: Double get() = (subtotal - discount).coerceAtLeast(0.0)
    val tax: Double get() = taxableAmount * 0.16
    val tip: Double get() = taxableAmount * (tipPercent / 100.0)
    val total: Double get() = taxableAmount + tax + tip
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: PosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // Mantenemos la lista de clientes general separada ya que no es exclusiva del "estado" del carrito
    val customers: StateFlow<List<Customer>> = repository.getCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Backward compatibility getters for easy migration, though uiState is preferred
    val cartItems: StateFlow<List<CartItem>> = _uiState.map { it.items }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    val tipPercent: StateFlow<Double> = _uiState.map { it.tipPercent }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)
    val discountPercent: StateFlow<Double> = _uiState.map { it.discountPercent }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)
    val selectedServiceType: StateFlow<ServiceType> = _uiState.map { it.selectedServiceType }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), ServiceType.DINE_IN)
    val selectedCartCustomer: StateFlow<Customer?> = _uiState.map { it.selectedCustomer }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val cartSubtotal: Double get() = _uiState.value.subtotal
    val cartDiscount: Double get() = _uiState.value.discount
    val cartTax: Double get() = _uiState.value.tax
    val cartTip: Double get() = _uiState.value.tip
    val cartTotal: Double get() = _uiState.value.total

    fun setServiceType(type: ServiceType) {
        _uiState.update { it.copy(selectedServiceType = type) }
    }

    fun setCartCustomer(customer: Customer?) {
        _uiState.update { it.copy(selectedCustomer = customer) }
    }
    
    fun setTipPercent(percent: Double) {
        _uiState.update { it.copy(tipPercent = percent) }
    }

    fun setDiscountPercent(percent: Double) {
        _uiState.update { it.copy(discountPercent = percent) }
    }

    fun scanAndAddToCart(
        barcode: String,
        onFound: (Product) -> Unit,
        onNotFound: (String) -> Unit
    ) {
        viewModelScope.launch {
            val product = repository.getProductByBarcode(barcode)
            if (product != null) {
                addToCart(product)
                onFound(product)
            } else {
                onNotFound(barcode)
            }
        }
    }

    fun addToCart(
        product: Product,
        selectedVariant: ProductVariant? = null,
        selectedModifiers: List<ProductModifier> = emptyList()
    ) {
        _uiState.update { currentState ->
            val existingIndex = currentState.items.indexOfFirst {
                it.product.id == product.id &&
                        it.selectedVariant?.id == selectedVariant?.id &&
                        it.selectedModifiers == selectedModifiers
            }
            val newItems = if (existingIndex != -1) {
                currentState.items.mapIndexed { index, item ->
                    if (index == existingIndex) item.copy(quantity = item.quantity + 1) else item
                }
            } else {
                currentState.items + CartItem(
                    product = product,
                    quantity = 1,
                    selectedVariant = selectedVariant,
                    selectedModifiers = selectedModifiers
                )
            }
            currentState.copy(items = newItems)
        }
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        _uiState.update { currentState ->
            val newItems = if (newQuantity <= 0) {
                currentState.items.filter { it.product.id != productId }
            } else {
                currentState.items.map { if (it.product.id == productId) it.copy(quantity = newQuantity) else it }
            }
            currentState.copy(items = newItems)
        }
    }

    fun clearCart() {
        _uiState.value = CartUiState()
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
                _uiState.update { it.copy(items = converted, activeTableId = tableId) }
                onTransferred()
            }
        }
    }

    fun processCheckout(method: PaymentMethod, customerId: String? = null): Order {
        val state = _uiState.value
        val currentItems = state.items.toList()
        val customerObj = customers.value.find { it.id == customerId } ?: state.selectedCustomer

        val newOrder = Order(
            id = System.currentTimeMillis().toString(),
            items = currentItems,
            subtotal = state.subtotal,
            discount = state.discount,
            tax = state.tax,
            tip = state.tip,
            total = state.total,
            status = OrderStatus.COMPLETED,
            paymentMethod = method,
            customerId = customerObj?.id,
            customerName = customerObj?.name,
            serviceType = state.selectedServiceType
        )

        viewModelScope.launch {
            // Delegamos la complejidad a la función atómica del repositorio
            repository.processCheckoutAtomic(newOrder, state.activeTableId)
        }
        
        clearCart()
        return newOrder
    }
}
