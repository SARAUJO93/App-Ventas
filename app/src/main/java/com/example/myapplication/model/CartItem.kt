package com.example.myapplication.model

data class CartItem(
    val product: Product,
    var quantity: Int,
    var notes: String = ""
) {
    val total: Double
        get() = product.price * quantity
}