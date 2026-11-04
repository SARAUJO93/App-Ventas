package com.example.myapplication.model

data class CartItem(
    val product: Product,
    var quantity: Int,
    val selectedVariant: ProductVariant? = null,
    val selectedModifiers: List<ProductModifier> = emptyList(),
    var notes: String = ""
) {
    val unitPrice: Double
        get() = (selectedVariant?.price ?: product.price) + selectedModifiers.sumOf { it.price }

    val total: Double
        get() = unitPrice * quantity
}