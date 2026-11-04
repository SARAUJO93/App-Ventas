package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.PosDao
import com.example.myapplication.data.local.PosDatabase
import com.example.myapplication.data.repository.PosRepository
import com.example.myapplication.data.repository.PosRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PosDatabase {
        return Room.databaseBuilder(
            context,
            PosDatabase::class.java,
            "nova_pos_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePosDao(db: PosDatabase): PosDao {
        return db.posDao()
    }

    @Provides
    @Singleton
    fun providePosRepository(dao: PosDao): PosRepository {
        return PosRepositoryImpl(dao)
    }
}