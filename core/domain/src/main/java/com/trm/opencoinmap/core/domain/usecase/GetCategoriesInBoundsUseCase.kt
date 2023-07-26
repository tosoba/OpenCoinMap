package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class GetCategoriesInBoundsUseCase @Inject constructor(private val repo: VenueRepo) {
  operator fun invoke(mapBounds: MapBounds): Flowable<List<VenueCategoryCount>> =
    repo.getCategoriesInBounds(mapBounds)
}
