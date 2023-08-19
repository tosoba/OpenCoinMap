package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class GetCategoriesWithCountInBoundsUseCase @Inject constructor(private val repo: VenueRepo) {
  operator fun invoke(mapBounds: List<MapBounds>): Flowable<List<VenueCategoryCount>> =
    repo.getCategoriesWithCountInBounds(mapBounds)
}
