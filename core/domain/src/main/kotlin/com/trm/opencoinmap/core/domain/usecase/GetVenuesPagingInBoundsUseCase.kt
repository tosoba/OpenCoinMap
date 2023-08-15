package com.trm.opencoinmap.core.domain.usecase

import androidx.paging.PagingData
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class GetVenuesPagingInBoundsUseCase @Inject constructor(private val repo: VenueRepo) {
  operator fun invoke(mapBounds: List<MapBounds>, query: String): Flowable<PagingData<Venue>> =
    repo.getVenuesPagingInBounds(mapBounds, query)
}
