package com.example.myapplication.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val stock: Int,
    val colorHex: Long = 0xFF007AFF,
    val barcode: String? = null
)