package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.MapCenter
import com.trm.opencoinmap.core.domain.repo.PreferenceDataSource
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class SaveMapCenterUseCase
@Inject
constructor(private val dataSource: PreferenceDataSource) {
  operator fun invoke(mapCenter: MapCenter): Completable = dataSource.saveMapCenter(mapCenter)
}
