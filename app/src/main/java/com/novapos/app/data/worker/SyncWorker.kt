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
            Log.d("SyncWorker", "Sincronización en segundo plano iniciada con conectividad de red activa")
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
