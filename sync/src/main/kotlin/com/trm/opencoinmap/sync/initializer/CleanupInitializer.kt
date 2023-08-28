package com.trm.opencoinmap.sync.initializer

import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkManagerInitializer
import com.trm.opencoinmap.sync.CleanupWorker

object Cleanup {
  fun initialize(context: Context) {
    AppInitializer.getInstance(context).initializeComponent(CleanupInitializer::class.java)
  }
}

class CleanupInitializer : Initializer<Cleanup> {
  override fun create(context: Context): Cleanup {
    WorkManager.getInstance(context)
      .enqueueUniquePeriodicWork(
        CleanupWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        CleanupWorker.workRequest(),
      )
    return Cleanup
  }

  override fun dependencies(): List<Class<out Initializer<*>>> =
    listOf(WorkManagerInitializer::class.java)
}
