package com.example.myapplication.data.repository

import com.example.myapplication.data.local.*
import com.example.myapplication.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface PosRepository {
    fun getProducts(): Flow<List<Product>>
    fun getTables(): Flow<List<Table>>
    fun getCustomers(): Flow<List<Customer>>
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(productId: String)
    suspend fun deductStock(cartItems: List<CartItem>)
    suspend fun populateInitialData()
}

@Singleton
class PosRepositoryImpl @Inject constructor(
    private val dao: PosDao
) : PosRepository {

    override fun getProducts(): Flow<List<Product>> {
        return dao.getAllProducts().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getTables(): Flow<List<Table>> {
        return dao.getAllTables().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getCustomers(): Flow<List<Customer>> {
        return dao.getAllCustomers().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun addProduct(product: Product) {
        dao.insertProduct(product.toEntity())
    }

    override suspend fun updateProduct(product: Product) {
        dao.updateProduct(product.toEntity())
    }

    override suspend fun deleteProduct(productId: String) {
        dao.deleteProductById(productId)
    }

    override suspend fun deductStock(cartItems: List<CartItem>) {
        cartItems.forEach { item ->
            val entity = dao.getProductById(item.product.id)
            if (entity != null) {
                val newStock = (entity.stock - item.quantity).coerceAtLeast(0)
                dao.updateProduct(entity.copy(stock = newStock))
            }
        }
    }

    override suspend fun populateInitialData() {
        val existingProducts = dao.getAllProducts().first()
        if (existingProducts.isEmpty()) {
            val defaultProducts = listOf(
                Product("1", "Café Americano", 35.0, "Bebidas", 50, 0xFF8D6E63).toEntity(),
                Product("2", "Capuccino", 45.0, "Bebidas", 40, 0xFF8D6E63).toEntity(),
                Product("3", "Croissant", 30.0, "Panadería", 20, 0xFFFFB74D).toEntity(),
                Product("4", "Sándwich de Pavo", 75.0, "Alimentos", 15, 0xFF81C784).toEntity(),
                Product("5", "Agua Mineral", 25.0, "Bebidas", 100, 0xFF4FC3F7).toEntity(),
                Product("6", "Cheesecake", 55.0, "Postres", 10, 0xFFF06292).toEntity()
            )
            dao.insertProducts(defaultProducts)
        }

        val existingTables = dao.getAllTables().first()
        if (existingTables.isEmpty()) {
            val defaultTables = listOf(
                Table("1", "Mesa 1", TableStatus.OCCUPIED, 125.0, 45).toEntity(),
                Table("2", "Mesa 2", TableStatus.AVAILABLE).toEntity(),
                Table("3", "Mesa 3", TableStatus.WAITING_BILL, 350.0, 120).toEntity(),
                Table("4", "Mesa 4", TableStatus.AVAILABLE).toEntity(),
                Table("5", "Barra 1", TableStatus.OCCUPIED, 45.0, 15).toEntity()
            )
            dao.insertTables(defaultTables)
        }

        val existingCustomers = dao.getAllCustomers().first()
        if (existingCustomers.isEmpty()) {
            val defaultCustomers = listOf(
                Customer("1", "Juan Pérez", "555-1234", debtBalance = 150.0).toEntity(),
                Customer("2", "María García", "555-5678", debtBalance = 0.0).toEntity(),
                Customer("3", "Carlos López", "555-8765", debtBalance = 450.0).toEntity()
            )
            dao.insertCustomers(defaultCustomers)
        }
    }
}