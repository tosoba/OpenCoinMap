package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class GetVenuesCountUseCase @Inject constructor(private val repo: VenueRepo) {
  operator fun invoke(): Flowable<Int> = repo.getVenuesCount()
}
