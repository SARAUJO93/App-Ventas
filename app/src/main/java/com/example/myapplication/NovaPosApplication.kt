package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.repository.PosRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class NovaPosApplication : Application() {

    @Inject
    lateinit var repository: PosRepository

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            repository.populateInitialData()
        }
        com.example.myapplication.data.worker.SyncWorker.schedulePeriodicSync(this)
    }
}