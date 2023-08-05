package com.trm.opencoinmap.core.domain.repo

import com.trm.opencoinmap.core.domain.model.MapCenter
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

interface PreferenceDataSource {
  fun getMapCenter(): Maybe<MapCenter>

  fun saveMapCenter(mapCenter: MapCenter): Completable
}
