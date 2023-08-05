package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.MapCenter
import com.trm.opencoinmap.core.domain.repo.PreferenceDataSource
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject

class GetInitialMapCenterUseCase @Inject constructor(private val dataSource: PreferenceDataSource) {
  operator fun invoke(): Maybe<MapCenter> = dataSource.getMapCenter()
}
