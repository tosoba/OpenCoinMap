package com.trm.opencoinmap.sync.initializer

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.trm.opencoinmap.sync.SyncWorker

object Sync {
  fun initialize(context: Context) {
    AppInitializer.getInstance(context).initializeComponent(SyncInitializer::class.java)
  }
}

class SyncInitializer : Initializer<Sync> {
  override fun create(context: Context): Sync {
    WorkManager.getInstance(context).apply {
      enqueueUniquePeriodicWork(
        SyncWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        SyncWorker.workRequest(),
      )
    }
    return Sync
  }

  override fun dependencies(): List<Class<out Initializer<*>>> =
    listOf(WorkManagerInitializer::class.java)
}
