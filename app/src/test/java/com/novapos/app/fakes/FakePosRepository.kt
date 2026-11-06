package com.novapos.app.fakes

import com.example.myapplication.data.local.CashShiftEntity
import com.example.myapplication.data.local.EmployeeEntity
import com.example.myapplication.data.local.TableItemEntity
import com.example.myapplication.data.local.TransactionEntity
import com.example.myapplication.data.repository.PosRepository
import com.example.myapplication.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakePosRepository : PosRepository {

    private val products = mutableListOf<Product>()
    private val customersFlow = MutableStateFlow<List<Customer>>(emptyList())
    private val orders = mutableListOf<Order>()

    override fun getProducts(): Flow<List<Product>> = flowOf(products)
    override fun getTables(): Flow<List<Table>> = flowOf(emptyList())
    override fun getCustomers(): Flow<List<Customer>> = customersFlow
    override fun getTableItems(tableId: String): Flow<List<TableItemEntity>> = flowOf(emptyList())
    override fun getOrders(): Flow<List<Order>> = flowOf(orders)
    override fun getActiveShift(): Flow<CashShiftEntity?> = flowOf(null)
    override fun getAllShifts(): Flow<List<CashShiftEntity>> = flowOf(emptyList())
    override fun getTransactions(): Flow<List<TransactionEntity>> = flowOf(emptyList())

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return products.find { it.barcode == barcode }
    }

    override suspend fun authenticatePin(pin: String): EmployeeEntity? = null

    override suspend fun addProduct(product: Product) {
        products.add(product)
    }

    override suspend fun updateProduct(product: Product) {}
    override suspend fun deleteProduct(productId: String) {}
    override suspend fun updateTable(table: Table) {}
    override suspend fun addItemToTable(tableId: String, product: Product, quantity: Int) {}
    override suspend fun removeItemFromTable(tableId: String, productId: String) {}
    override suspend fun clearTableOrder(tableId: String) {}

    override suspend fun addCustomer(customer: Customer) {
        customersFlow.value = customersFlow.value + customer
    }

    override suspend fun updateCustomer(customer: Customer) {}
    override suspend fun deleteCustomer(customerId: String) {}
    override suspend fun addCustomerDebt(customerId: String, amount: Double) {}
    override suspend fun recordCustomerPayment(customerId: String, paymentAmount: Double) {}
    override suspend fun saveOrder(order: Order) {
        orders.add(order)
    }

    override suspend fun processCheckoutAtomic(order: Order, tableIdToClear: String?): Order {
        orders.add(order)
        return order
    }

    override suspend fun openShift(cashierName: String, initialCash: Double) {}
    override suspend fun closeShift(shiftId: String, actualCash: Double) {}
    override suspend fun recordSaleInActiveShift(amount: Double, paymentMethod: PaymentMethod) {}
    override suspend fun addTransaction(type: String, amount: Double, category: String, note: String) {}
    override suspend fun deductStock(cartItems: List<CartItem>) {}
    override suspend fun populateInitialData() {}
}