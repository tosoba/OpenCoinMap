package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.sync.SyncDataSource
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class IsVenuesSyncRunningUseCase @Inject constructor(private val syncDataSource: SyncDataSource) {
  operator fun invoke(): Observable<Boolean> = syncDataSource.isRunningObservable()
}
