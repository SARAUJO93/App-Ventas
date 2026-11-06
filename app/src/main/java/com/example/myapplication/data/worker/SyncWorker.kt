package com.example.myapplication.data.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.myapplication.data.local.PosDao
import com.example.myapplication.data.remote.FirestoreSyncManager
import com.example.myapplication.model.Order
import com.example.myapplication.model.OrderStatus
import com.example.myapplication.model.PaymentMethod
import com.example.myapplication.model.ServiceType
import kotlinx.coroutines.flow.first
import java.util.Date

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("SyncWorker", "Iniciando sincronización periódica de pendientes en segundo plano...")
            // WorkManager ejecutará doWork en segundo plano automáticamente cuando haya conexión
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error durante sincronización en segundo plano", e)
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