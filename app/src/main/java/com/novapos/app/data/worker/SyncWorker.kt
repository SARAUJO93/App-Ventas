package com.novapos.app.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.novapos.app.data.local.PosDao
import com.novapos.app.data.remote.FirestoreSyncManager
import com.novapos.app.model.Order
import com.novapos.app.model.OrderStatus
import com.novapos.app.model.PaymentMethod
import com.novapos.app.model.ServiceType
import kotlinx.coroutines.flow.first
import java.util.Date

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "Iniciando procesamiento de cola de ventas offline pendientes...")
            val db = androidx.room.Room.databaseBuilder(
                applicationContext,
                com.novapos.app.data.local.PosDatabase::class.java,
                "nova_pos_db"
            ).fallbackToDestructiveMigration().build()

            val dao = db.posDao()
            val syncManager = FirestoreSyncManager()

            val unsyncedOrders = dao.getUnsyncedOrders()
            if (unsyncedOrders.isNotEmpty()) {
                Log.d("SyncWorker", "Se encontraron ${unsyncedOrders.size} órdenes pendientes de sincronizar")
                for (entity in unsyncedOrders) {
                    val itemEntities = dao.getOrderItems(entity.id)
                    val items = itemEntities.map { item ->
                        com.novapos.app.model.CartItem(
                            product = com.novapos.app.model.Product(
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

                    val order = Order(
                        id = entity.id,
                        items = items,
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
                        serviceType = ServiceType.valueOf(entity.serviceType)
                    )

                    val isSuccess = syncManager.syncOrderToCloud(order)
                    if (isSuccess) {
                        dao.markOrderAsSynced(entity.id)
                        Log.d("SyncWorker", "Orden ${entity.id} marcada como sincronizada exitosamente")
                    } else {
                        Log.w("SyncWorker", "No se pudo sincronizar la orden ${entity.id} a la nube. Permanece en la cola local.")
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error en sincronización en segundo plano", e)
            Result.retry()
        }
    }

    companion object {
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "OfflineSyncWork",
                ExistingWorkPolicy.KEEP,
                syncRequest
            )
        }
    }
}
