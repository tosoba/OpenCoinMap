package com.trm.opencoinmap

import android.app.Application
import com.trm.opencoinmap.sync.initializer.Sync
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import timber.log.Timber

@HiltAndroidApp
class OpenCoinMapApp : Application() {
  override fun onCreate() {
    super.onCreate()
    initTimber()
    initSync()
    initOsm()
  }

  private fun initSync() {
    Sync.initialize(context = this)
  }

  private fun initOsm() {
    Configuration.getInstance().userAgentValue = packageName
  }

  private fun initTimber() {
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
  }
}
