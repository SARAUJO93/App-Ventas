package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.model.Customer
import com.example.myapplication.model.Product
import com.example.myapplication.model.Table
import com.example.myapplication.model.TableStatus

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val stock: Int,
    val colorHex: Long
)

fun ProductEntity.toDomain() = Product(id, name, price, category, stock, colorHex)
fun Product.toEntity() = ProductEntity(id, name, price, category, stock, colorHex)

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey val id: String,
    val name: String,
    val status: String,
    val currentOrderTotal: Double,
    val occupancyTimeMinutes: Int
)

fun TableEntity.toDomain() = Table(
    id = id,
    name = name,
    status = TableStatus.valueOf(status),
    currentOrderTotal = currentOrderTotal,
    occupancyTimeMinutes = occupancyTimeMinutes
)

fun Table.toEntity() = TableEntity(
    id = id,
    name = name,
    status = status.name,
    currentOrderTotal = currentOrderTotal,
    occupancyTimeMinutes = occupancyTimeMinutes
)

@Entity(tableName = "table_items")
data class TableItemEntity(
    @PrimaryKey val id: String,
    val tableId: String,
    val productId: String,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val colorHex: Long
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val debtBalance: Double
)

fun CustomerEntity.toDomain() = Customer(id, name, phone, email, debtBalance)
fun Customer.toEntity() = CustomerEntity(id, name, phone, email, debtBalance)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val tip: Double,
    val total: Double,
    val status: String,
    val dateTimestamp: Long,
    val paymentMethod: String?,
    val customerId: String?,
    val customerName: String?
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val colorHex: Long
)

@Entity(tableName = "cash_shifts")
data class CashShiftEntity(
    @PrimaryKey val id: String,
    val cashierName: String,
    val openTimestamp: Long,
    val closeTimestamp: Long? = null,
    val initialCash: Double,
    val cashSales: Double = 0.0,
    val cardSales: Double = 0.0,
    val transferSales: Double = 0.0,
    val creditSales: Double = 0.0,
    val expectedCash: Double = initialCash,
    val actualCash: Double = 0.0,
    val status: String = "OPEN"
)

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val pin: String,
    val role: String
)