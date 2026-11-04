package com.example.myapplication.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)
}