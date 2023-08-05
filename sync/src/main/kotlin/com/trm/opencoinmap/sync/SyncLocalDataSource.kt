package com.trm.opencoinmap.sync

import com.jakewharton.rxrelay3.BehaviorRelay
import com.trm.opencoinmap.core.domain.sync.SyncDataSource
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncLocalDataSource @Inject constructor() : SyncDataSource {
  private val syncRunning = BehaviorRelay.createDefault(false)

  override var isRunning: Boolean
    get() = syncRunning.value ?: false
    set(value) {
      syncRunning.accept(value)
    }

  override fun isRunningObservable(): Observable<Boolean> = syncRunning
}
