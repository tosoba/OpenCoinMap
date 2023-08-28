package com.trm.opencoinmap

import android.app.Application
import com.trm.opencoinmap.sync.initializer.Cleanup
import com.trm.opencoinmap.sync.initializer.Sync
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import org.osmdroid.config.Configuration
import timber.log.Timber

@HiltAndroidApp
class OpenCoinMapApp : Application() {
  override fun onCreate() {
    super.onCreate()
    initTimber()
    initRxErrorHandler()
    initWorkers()
    initOsm()
  }

  private fun initTimber() {
    if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
  }

  private fun initRxErrorHandler() {
    RxJavaPlugins.setErrorHandler { Timber.tag("RX").e(it) }
  }

  private fun initWorkers() {
    Sync.initialize(this)
    Cleanup.initialize(this)
  }

  private fun initOsm() {
    Configuration.getInstance().userAgentValue = packageName
  }
}
