package com.novapos.app.model

data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String = "",
    val debtBalance: Double = 0.0
)
