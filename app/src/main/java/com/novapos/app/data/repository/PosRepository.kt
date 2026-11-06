package com.novapos.app.data.repository

import com.novapos.app.data.local.*
import com.novapos.app.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface PosRepository {
    fun getProducts(): Flow<List<Product>>
    fun getTables(): Flow<List<Table>>
    fun getCustomers(): Flow<List<Customer>>
    fun getTableItems(tableId: String): Flow<List<TableItemEntity>>
    fun getOrders(): Flow<List<Order>>
    fun getActiveShift(): Flow<CashShiftEntity?>
    fun getAllShifts(): Flow<List<CashShiftEntity>>
    fun getTransactions(): Flow<List<TransactionEntity>>
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun authenticatePin(pin: String): EmployeeEntity?
    suspend fun addProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(productId: String)
    suspend fun updateTable(table: Table)
    suspend fun addItemToTable(tableId: String, product: Product, quantity: Int = 1)
    suspend fun removeItemFromTable(tableId: String, productId: String)
    suspend fun clearTableOrder(tableId: String)
    suspend fun addCustomer(customer: Customer)
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(customerId: String)
    suspend fun addCustomerDebt(customerId: String, amount: Double)
    suspend fun recordCustomerPayment(customerId: String, paymentAmount: Double)
    suspend fun saveOrder(order: Order) // Lo mantenemos por si se usa en otro lado, pero desaconsejado para checkout
    suspend fun processCheckoutAtomic(order: Order, tableIdToClear: String?): Order
    suspend fun openShift(cashierName: String, initialCash: Double)
    suspend fun closeShift(shiftId: String, actualCash: Double)
    suspend fun recordSaleInActiveShift(amount: Double, paymentMethod: PaymentMethod)
    suspend fun addTransaction(type: String, amount: Double, category: String, note: String)
    suspend fun deductStock(cartItems: List<CartItem>)
    suspend fun createAdminEmployee(adminName: String, pin: String)
    suspend fun populateInitialData()
}

@Singleton
class PosRepositoryImpl @Inject constructor(
    private val dao: PosDao,
    private val syncManager: com.novapos.app.data.remote.FirestoreSyncManager
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

    override fun getTableItems(tableId: String): Flow<List<TableItemEntity>> {
        return dao.getTableItems(tableId)
    }

    override fun getOrders(): Flow<List<Order>> {
        return dao.getAllOrders().map { orderEntities ->
            orderEntities.map { entity ->
                val itemEntities = dao.getOrderItems(entity.id)
                val cartItems = itemEntities.map { item ->
                    CartItem(
                        product = Product(
                            id = item.productId,
                            name = item.productName,
                            price = item.unitPrice,
                            category = "Venta",
                            stock = 999,
                            colorHex = item.colorHex
                        ),
                        quantity = item.quantity
                    )
                }
                Order(
                    id = entity.id,
                    items = cartItems,
                    subtotal = entity.subtotal,
                    discount = entity.discount,
                    tax = entity.tax,
                    tip = entity.tip,
                    total = entity.total,
                    status = OrderStatus.valueOf(entity.status),
                    date = Date(entity.dateTimestamp),
                    paymentMethod = entity.paymentMethod?.let { PaymentMethod.valueOf(it) },
                    customerId = entity.customerId,
                    customerName = entity.customerName,
                    serviceType = try { ServiceType.valueOf(entity.serviceType) } catch (e: Exception) { ServiceType.DINE_IN }
                )
            }
        }
    }

    override fun getActiveShift(): Flow<CashShiftEntity?> {
        return dao.getActiveShift()
    }

    override fun getAllShifts(): Flow<List<CashShiftEntity>> {
        return dao.getAllShifts()
    }

    override fun getTransactions(): Flow<List<TransactionEntity>> {
        return dao.getAllTransactions()
    }

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return dao.getProductByBarcode(barcode)?.toDomain()
    }

    override suspend fun authenticatePin(pin: String): EmployeeEntity? {
        val employees = dao.getAllEmployees().first()
        return employees.find { emp ->
            com.novapos.app.util.SecurityUtils.verifyPin(pin, emp.pin)
        }
    }

    override suspend fun openShift(cashierName: String, initialCash: Double) {
        val newShift = CashShiftEntity(
            id = UUID.randomUUID().toString(),
            cashierName = cashierName,
            openTimestamp = System.currentTimeMillis(),
            initialCash = initialCash,
            expectedCash = initialCash,
            status = "OPEN"
        )
        dao.insertShift(newShift)
    }

    override suspend fun closeShift(shiftId: String, actualCash: Double) {
        val currentShift = dao.getAllShifts().first().find { it.id == shiftId }
        if (currentShift != null) {
            val updated = currentShift.copy(
                actualCash = actualCash,
                closeTimestamp = System.currentTimeMillis(),
                status = "CLOSED"
            )
            dao.updateShift(updated)
        }
    }

    override suspend fun recordSaleInActiveShift(amount: Double, paymentMethod: PaymentMethod) {
        val activeShift = dao.getActiveShift().first()
        if (activeShift != null) {
            val updatedShift = when (paymentMethod) {
                PaymentMethod.CASH -> activeShift.copy(
                    cashSales = activeShift.cashSales + amount,
                    expectedCash = activeShift.expectedCash + amount
                )
                PaymentMethod.CARD -> activeShift.copy(
                    cardSales = activeShift.cardSales + amount
                )
                PaymentMethod.TRANSFER -> activeShift.copy(
                    transferSales = activeShift.transferSales + amount
                )
                PaymentMethod.CREDIT -> activeShift.copy(
                    creditSales = activeShift.creditSales + amount
                )
            }
            dao.updateShift(updatedShift)
        }
    }

    override suspend fun addTransaction(type: String, amount: Double, category: String, note: String) {
        val transaction = TransactionEntity(
            id = UUID.randomUUID().toString(),
            type = type,
            amount = amount,
            category = category,
            note = note,
            timestamp = System.currentTimeMillis()
        )
        dao.insertTransaction(transaction)

        // Si la transacción es ingreso o gasto manual en efectivo, ajustar el esperado en caja
        val activeShift = dao.getActiveShift().first()
        if (activeShift != null) {
            val adjustment = if (type == "INCOME") amount else -amount
            dao.updateShift(activeShift.copy(expectedCash = activeShift.expectedCash + adjustment))
        }
    }

    override suspend fun saveOrder(order: Order) {
        val orderEntity = OrderEntity(
            id = order.id,
            subtotal = order.subtotal,
            discount = order.discount,
            tax = order.tax,
            tip = order.tip,
            total = order.total,
            status = order.status.name,
            dateTimestamp = order.date.time,
            paymentMethod = order.paymentMethod?.name,
            customerId = order.customerId,
            customerName = order.customerName,
            serviceType = order.serviceType.name
        )
        dao.insertOrder(orderEntity)

        val itemEntities = order.items.map { item ->
            OrderItemEntity(
                id = UUID.randomUUID().toString(),
                orderId = order.id,
                productId = item.product.id,
                productName = item.product.name,
                unitPrice = item.product.price,
                quantity = item.quantity,
                colorHex = item.product.colorHex
            )
        }
        dao.insertOrderItems(itemEntities)
    }

    override suspend fun processCheckoutAtomic(order: Order, tableIdToClear: String?): Order {
        val orderEntity = OrderEntity(
            id = order.id,
            subtotal = order.subtotal,
            discount = order.discount,
            tax = order.tax,
            tip = order.tip,
            total = order.total,
            status = order.status.name,
            dateTimestamp = order.date.time,
            paymentMethod = order.paymentMethod?.name,
            customerId = order.customerId,
            customerName = order.customerName,
            serviceType = order.serviceType.name
        )

        val itemEntities = order.items.map { item ->
            OrderItemEntity(
                id = UUID.randomUUID().toString(),
                orderId = order.id,
                productId = item.product.id,
                productName = item.product.name,
                unitPrice = item.product.price,
                quantity = item.quantity,
                colorHex = item.product.colorHex
            )
        }

        // 1. Preparar Deduciones de Stock
        val stockDeductions = mutableMapOf<String, Int>()
        for (item in order.items) {
            stockDeductions[item.product.id] = (stockDeductions[item.product.id] ?: 0) + item.quantity
        }

        // 2. Preparar Actualización de Turno Activo
        var shiftUpdate: CashShiftEntity? = null
        val activeShift = dao.getActiveShift().first()
        if (activeShift != null && order.paymentMethod != null) {
            val amount = order.total
            shiftUpdate = when (order.paymentMethod) {
                PaymentMethod.CASH -> activeShift.copy(
                    cashSales = activeShift.cashSales + amount,
                    expectedCash = activeShift.expectedCash + amount
                )
                PaymentMethod.CARD -> activeShift.copy(cardSales = activeShift.cardSales + amount)
                PaymentMethod.TRANSFER -> activeShift.copy(transferSales = activeShift.transferSales + amount)
                PaymentMethod.CREDIT -> activeShift.copy(creditSales = activeShift.creditSales + amount)
            }
        }

        // 3. Preparar Actualización de Fiado/Deuda (Si aplica)
        var customerUpdate: CustomerEntity? = null
        if (order.paymentMethod == PaymentMethod.CREDIT && order.customerId != null) {
            val customer = dao.getCustomerById(order.customerId)
            if (customer != null) {
                customerUpdate = customer.copy(debtBalance = customer.debtBalance + order.total)
            }
        }

        // 4. Enviar todo al Transaction Atómico de Room
        dao.processSaleAtomic(
            order = orderEntity,
            orderItems = itemEntities,
            stockDeductions = stockDeductions,
            shiftUpdate = shiftUpdate,
            customerUpdate = customerUpdate,
            tableIdToClear = tableIdToClear
        )

        // Si venía de mesa, actualizamos el status de la mesa a AVAILABLE
        if (tableIdToClear != null) {
            val tables = dao.getAllTables().first()
            val currentTable = tables.find { it.id == tableIdToClear }
            if (currentTable != null) {
                dao.updateTable(currentTable.copy(currentOrderTotal = 0.0, status = TableStatus.AVAILABLE.name))
            }
        }

        // 5. Sincronizar en la Nube (Cloud Firestore Atómico con confirmación de estado)
        try {
            val isSynced = syncManager.syncOrderToCloud(order)
            if (isSynced) {
                dao.markOrderAsSynced(order.id)
            }
            for (item in order.items) {
                syncManager.deductStockAtomicInCloud(item.product.id, item.quantity)
            }
        } catch (e: Exception) {
            android.util.Log.e("PosRepository", "Error durante sincronización directa de orden ${order.id}", e)
        }

        return order
    }

    override suspend fun addProduct(product: Product) {
        dao.insertProduct(product.toEntity())
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try { syncManager.syncProductToCloud(product) } catch (e: Exception) { android.util.Log.e("PosRepository", "Error syncing product", e) }
        }
    }

    override suspend fun updateProduct(product: Product) {
        dao.updateProduct(product.toEntity())
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try { syncManager.syncProductToCloud(product) } catch (e: Exception) { android.util.Log.e("PosRepository", "Error syncing product", e) }
        }
    }

    override suspend fun deleteProduct(productId: String) {
        dao.deleteProductById(productId)
    }

    override suspend fun updateTable(table: Table) {
        dao.updateTable(table.toEntity())
    }

    override suspend fun addItemToTable(tableId: String, product: Product, quantity: Int) {
        val existing = dao.getTableItem(tableId, product.id)
        if (existing != null) {
            val newQty = existing.quantity + quantity
            dao.updateTableItem(existing.copy(quantity = newQty))
        } else {
            val newItem = TableItemEntity(
                id = UUID.randomUUID().toString(),
                tableId = tableId,
                productId = product.id,
                productName = product.name,
                unitPrice = product.price,
                quantity = quantity,
                colorHex = product.colorHex
            )
            dao.insertTableItem(newItem)
        }
        recalculateTableTotal(tableId)
    }

    override suspend fun removeItemFromTable(tableId: String, productId: String) {
        val existing = dao.getTableItem(tableId, productId)
        if (existing != null) {
            if (existing.quantity > 1) {
                dao.updateTableItem(existing.copy(quantity = existing.quantity - 1))
            } else {
                dao.deleteTableItem(tableId, productId)
            }
        }
        recalculateTableTotal(tableId)
    }

    override suspend fun clearTableOrder(tableId: String) {
        dao.clearTableItems(tableId)
        val tables = dao.getAllTables().first()
        val currentTable = tables.find { it.id == tableId }
        if (currentTable != null) {
            dao.updateTable(currentTable.copy(currentOrderTotal = 0.0, status = TableStatus.AVAILABLE.name))
        }
    }

    override suspend fun addCustomer(customer: Customer) {
        dao.insertCustomer(customer.toEntity())
    }

    override suspend fun updateCustomer(customer: Customer) {
        dao.updateCustomer(customer.toEntity())
    }

    override suspend fun deleteCustomer(customerId: String) {
        dao.deleteCustomerById(customerId)
    }

    override suspend fun addCustomerDebt(customerId: String, amount: Double) {
        val customer = dao.getCustomerById(customerId)
        if (customer != null) {
            dao.updateCustomer(customer.copy(debtBalance = customer.debtBalance + amount))
        }
    }

    override suspend fun recordCustomerPayment(customerId: String, paymentAmount: Double) {
        val customer = dao.getCustomerById(customerId)
        if (customer != null) {
            val newDebt = (customer.debtBalance - paymentAmount).coerceAtLeast(0.0)
            dao.updateCustomer(customer.copy(debtBalance = newDebt))
        }
    }

    private suspend fun recalculateTableTotal(tableId: String) {
        val items = dao.getTableItems(tableId).first()
        val total = items.sumOf { it.unitPrice * it.quantity }
        val tables = dao.getAllTables().first()
        val currentTable = tables.find { it.id == tableId }
        if (currentTable != null) {
            val newStatus = if (total > 0 && currentTable.status == TableStatus.AVAILABLE.name) {
                TableStatus.OCCUPIED.name
            } else currentTable.status
            dao.updateTable(currentTable.copy(currentOrderTotal = total, status = newStatus))
        }
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

    override suspend fun createAdminEmployee(adminName: String, pin: String) {
        val hashed = com.novapos.app.util.SecurityUtils.hashPin(pin)
        val admin = EmployeeEntity(
            id = "1",
            name = if (adminName.isBlank()) "Administrador" else adminName,
            pin = hashed,
            role = "ADMIN"
        )
        dao.insertEmployees(listOf(admin))
    }

    override suspend fun populateInitialData() {
        val existingProducts = dao.getAllProducts().first()
        if (existingProducts.isEmpty()) {
            val defaultProducts = listOf(
                Product("1", "Café Americano", 35.0, "Bebidas", 50, 0xFF8D6E63, "7501234567890").toEntity(),
                Product("2", "Capuccino", 45.0, "Bebidas", 40, 0xFF8D6E63, "7501234567891").toEntity(),
                Product("3", "Croissant", 30.0, "Panadería", 20, 0xFFFFB74D, "7501234567892").toEntity(),
                Product("4", "Sándwich de Pavo", 75.0, "Alimentos", 15, 0xFF81C784, "7501234567893").toEntity(),
                Product("5", "Agua Mineral", 25.0, "Bebidas", 100, 0xFF4FC3F7, "7501234567894").toEntity(),
                Product("6", "Cheesecake", 55.0, "Postres", 10, 0xFFF06292, "7501234567895").toEntity()
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
