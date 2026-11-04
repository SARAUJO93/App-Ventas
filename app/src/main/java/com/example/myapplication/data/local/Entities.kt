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