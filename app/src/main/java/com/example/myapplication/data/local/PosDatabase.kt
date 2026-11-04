package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        TableEntity::class,
        TableItemEntity::class,
        CustomerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PosDatabase : RoomDatabase() {
    abstract fun posDao(): PosDao
}