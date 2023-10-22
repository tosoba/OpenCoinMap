package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.domain.sync.SyncDataSource
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class IsVenuesSyncRunningAndNoVenuesExistUseCase
@Inject
constructor(
  private val syncDataSource: SyncDataSource,
  private val venueRepo: VenueRepo,
) {
  operator fun invoke(): Flowable<Boolean> =
    Flowable.combineLatest(
      syncDataSource.isRunningFlowable(),
      venueRepo.anyVenuesExist(),
    ) { isRunning, anyExist ->
      isRunning && !anyExist
    }
}
