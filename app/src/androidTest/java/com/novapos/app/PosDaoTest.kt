package com.novapos.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PosDaoTest {

    private lateinit var db: PosDatabase
    private lateinit var dao: PosDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, PosDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.posDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertProductAndGetByBarcode() = runBlocking {
        val product = ProductEntity("p1", "Cerveza Corona", 35.0, "Bebidas", 50, 0xFF4A90E2, barcode = "75010001")
        dao.insertProduct(product)

        val retrieved = dao.getProductByBarcode("75010001")
        assertNotNull(retrieved)
        assertEquals("Cerveza Corona", retrieved?.name)
        assertEquals(50, retrieved?.stock)
    }

    @Test
    fun processSaleAtomicDeductsStockAndInsertsOrder() = runBlocking {
        // 1. Inserción inicial
        val product = ProductEntity("p1", "Taco", 20.0, "Comida", stock = 10, colorHex = 0xFF4A90E2)
        dao.insertProduct(product)

        val order = OrderEntity(
            id = "o1",
            subtotal = 40.0,
            discount = 0.0,
            tax = 6.4,
            tip = 0.0,
            total = 46.4,
            status = "COMPLETED",
            dateTimestamp = System.currentTimeMillis(),
            paymentMethod = "CASH",
            customerId = null,
            customerName = null,
            serviceType = "DINE_IN"
        )
        val orderItems = listOf(
            OrderItemEntity("item1", "o1", "p1", "Taco", 20.0, 2, 0xFF4A90E2)
        )

        // 2. Ejecutar transacción atómica
        dao.processSaleAtomic(
            order = order,
            orderItems = orderItems,
            stockDeductions = mapOf("p1" to 2),
            shiftUpdate = null,
            customerUpdate = null,
            tableIdToClear = null
        )

        // 3. Verificar deducción de stock
        val updatedProduct = dao.getProductById("p1")
        assertNotNull(updatedProduct)
        assertEquals(8, updatedProduct?.stock) // Stock redujo de 10 a 8

        // 4. Verificar orden en la base de datos
        val orders = dao.getAllOrders().first()
        assertEquals(1, orders.size)
        assertEquals("o1", orders[0].id)

        val items = dao.getOrderItems("o1")
        assertEquals(1, items.size)
        assertEquals(2, items[0].quantity)
    }
}