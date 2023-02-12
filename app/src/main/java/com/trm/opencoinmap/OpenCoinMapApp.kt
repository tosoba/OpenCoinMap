package com.trm.opencoinmap

import android.app.Application
import org.osmdroid.config.Configuration

class OpenCoinMapApp : Application() {
  override fun onCreate() {
    super.onCreate()
    initializeOsm()
  }

  private fun initializeOsm() {
    Configuration.getInstance().userAgentValue = packageName
  }
}
