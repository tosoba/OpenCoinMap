package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class IsVenuesSyncRunningAndNoVenuesExistUseCase
@Inject
constructor(
  private val isVenuesSyncRunningUseCase: IsVenuesSyncRunningUseCase,
  private val venueRepo: VenueRepo,
) {
  operator fun invoke(): Flowable<Boolean> =
    Flowable.combineLatest(
      isVenuesSyncRunningUseCase(),
      venueRepo.anyVenuesExist(),
    ) { isRunning, anyExist ->
      isRunning && !anyExist
    }
}
