package com.example.myapplication.model

data class ProductVariant(
    val id: String,
    val name: String,
    val price: Double
)

data class ProductModifier(
    val id: String,
    val name: String,
    val price: Double = 0.0
)

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val stock: Int,
    val colorHex: Long = 0xFF007AFF,
    val barcode: String? = null,
    val variants: List<ProductVariant> = emptyList(),
    val modifiers: List<ProductModifier> = emptyList()
)