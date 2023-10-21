package com.trm.opencoinmap.core.domain.sync

import io.reactivex.rxjava3.core.Flowable

interface SyncDataSource {
  fun isRunningFlowable(): Flowable<Boolean>
}
