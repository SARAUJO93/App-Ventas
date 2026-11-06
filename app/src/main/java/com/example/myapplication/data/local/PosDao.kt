package com.example.myapplication.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("SELECT * FROM tables")
    fun getAllTables(): Flow<List<TableEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<TableEntity>)

    @Update
    suspend fun updateTable(table: TableEntity)

    @Query("SELECT * FROM table_items WHERE tableId = :tableId")
    fun getTableItems(tableId: String): Flow<List<TableItemEntity>>

    @Query("SELECT * FROM table_items WHERE tableId = :tableId AND productId = :productId")
    suspend fun getTableItem(tableId: String, productId: String): TableItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTableItem(item: TableItemEntity)

    @Update
    suspend fun updateTableItem(item: TableItemEntity)

    @Query("DELETE FROM table_items WHERE tableId = :tableId")
    suspend fun clearTableItems(tableId: String)

    @Query("DELETE FROM table_items WHERE tableId = :tableId AND productId = :productId")
    suspend fun deleteTableItem(tableId: String, productId: String)

    @Query("SELECT * FROM customers")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM orders ORDER BY dateTimestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity>

    @Query("SELECT * FROM cash_shifts WHERE status = 'OPEN' ORDER BY openTimestamp DESC LIMIT 1")
    fun getActiveShift(): Flow<CashShiftEntity?>

    @Query("SELECT * FROM cash_shifts ORDER BY openTimestamp DESC")
    fun getAllShifts(): Flow<List<CashShiftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: CashShiftEntity)

    @Update
    suspend fun updateShift(shift: CashShiftEntity)

    @Query("SELECT * FROM employees")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE pin = :pin LIMIT 1")
    suspend fun getEmployeeByPin(pin: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<EmployeeEntity>)

    @Query("SELECT * FROM cash_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    // ==== TRANSACCIONES ATÓMICAS ====

    @Transaction
    suspend fun processSaleAtomic(
        order: OrderEntity,
        orderItems: List<OrderItemEntity>,
        stockDeductions: Map<String, Int>, // Product ID to deduction quantity
        shiftUpdate: CashShiftEntity?,
        customerUpdate: CustomerEntity?,
        tableIdToClear: String?
    ) {
        // 1. Guardar Orden
        insertOrder(order)
        insertOrderItems(orderItems)

        // 2. Deducir Stock
        for ((productId, qtyToDeduct) in stockDeductions) {
            val product = getProductById(productId)
            if (product != null) {
                updateProduct(product.copy(stock = product.stock - qtyToDeduct))
            }
        }

        // 3. Actualizar Turno de Caja (si hay uno activo)
        if (shiftUpdate != null) {
            updateShift(shiftUpdate)
        }

        // 4. Actualizar Deuda del Cliente (si es fiado)
        if (customerUpdate != null) {
            updateCustomer(customerUpdate)
        }

        // 5. Limpiar Mesa (si venía de una mesa)
        if (tableIdToClear != null) {
            clearTableItems(tableIdToClear)
            // No podemos cambiar el status a AVAILABLE desde aquí fácilmente sin el TableEntity,
            // pero el repo lo puede manejar, o podemos simplemente limpiarla.
            // Para mantener atomicidad, limpiamos los items:
            clearTableItems(tableIdToClear)
        }
    }
}