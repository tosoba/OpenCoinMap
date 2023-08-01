package com.trm.opencoinmap.core.domain.sync

import io.reactivex.rxjava3.core.Observable

interface SyncDataSource {
  var isRunning: Boolean

  fun isRunningObservable(): Observable<Boolean>
}
