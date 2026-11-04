package com.example.myapplication.model

import java.util.Date

enum class OrderStatus {
    PENDING, COMPLETED, CANCELLED
}

enum class PaymentMethod {
    CASH, CARD, TRANSFER, CREDIT
}

data class Order(
    val id: String,
    val items: List<CartItem>,
    val subtotal: Double,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val tip: Double = 0.0,
    val total: Double,
    val status: OrderStatus,
    val date: Date = Date(),
    val paymentMethod: PaymentMethod? = null,
    val customerId: String? = null,
    val customerName: String? = null
)