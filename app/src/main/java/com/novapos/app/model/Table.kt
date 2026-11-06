package com.novapos.app.model

enum class TableStatus {
    AVAILABLE, OCCUPIED, WAITING_BILL
}

data class Table(
    val id: String,
    val name: String,
    val status: TableStatus = TableStatus.AVAILABLE,
    val currentOrderTotal: Double = 0.0,
    val occupancyTimeMinutes: Int = 0
)
