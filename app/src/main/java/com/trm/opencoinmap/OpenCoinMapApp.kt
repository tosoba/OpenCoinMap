package com.trm.opencoinmap

import android.app.Application
import com.trm.opencoinmap.sync.initializer.Sync
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class OpenCoinMapApp : Application() {
  override fun onCreate() {
    super.onCreate()
    initSync()
    initOsm()
  }

  private fun initSync() {
    Sync.initialize(context = this)
  }

  private fun initOsm() {
    Configuration.getInstance().userAgentValue = packageName
  }
}
