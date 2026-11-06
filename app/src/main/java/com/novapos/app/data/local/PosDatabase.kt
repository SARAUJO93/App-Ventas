package com.novapos.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        TableEntity::class,
        TableItemEntity::class,
        CustomerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        CashShiftEntity::class,
        EmployeeEntity::class,
        TransactionEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao
}
