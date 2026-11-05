package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.PosRepository
import com.example.myapplication.model.Product
import com.example.myapplication.model.ProductModifier
import com.example.myapplication.model.ProductVariant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: PosRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.getProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProduct(
        name: String,
        price: Double,
        category: String,
        stock: Int,
        colorHex: Long,
        barcode: String? = null,
        variants: List<ProductVariant> = emptyList(),
        modifiers: List<ProductModifier> = emptyList()
    ) {
        viewModelScope.launch {
            val newProduct = Product(
                id = UUID.randomUUID().toString(),
                name = name,
                price = price,
                category = category,
                stock = stock,
                colorHex = colorHex,
                barcode = barcode,
                variants = variants,
                modifiers = modifiers
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
}