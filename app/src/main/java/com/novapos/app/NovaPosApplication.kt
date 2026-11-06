package com.novapos.app

import android.app.Application
import com.novapos.app.data.repository.PosRepository
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
        com.novapos.app.data.worker.SyncWorker.schedulePeriodicSync(this)
    }
}
