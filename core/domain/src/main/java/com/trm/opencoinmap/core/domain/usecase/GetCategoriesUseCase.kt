package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(private val repo: VenueRepo) {
  operator fun invoke(): Flowable<List<VenueCategoryCount>> = repo.getCategories()
}
