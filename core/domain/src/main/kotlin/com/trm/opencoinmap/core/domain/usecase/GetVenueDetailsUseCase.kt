package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.VenueDetails
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject

class GetVenueDetailsUseCase @Inject constructor(private val repo: VenueRepo) {
  operator fun invoke(id: Long): Maybe<VenueDetails> = repo.getVenueDetails(id)
}
