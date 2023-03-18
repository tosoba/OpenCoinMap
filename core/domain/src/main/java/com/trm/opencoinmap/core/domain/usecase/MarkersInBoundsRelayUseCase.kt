package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class MarkersInBoundsRelayUseCase @Inject constructor(private val repo: VenueRepo) {
  operator fun invoke(bounds: GridMapBounds): Observable<Loadable<List<MapMarker>>> =
    repo
      .getVenueMarkersInLatLngBounds(bounds)
      .map(List<MapMarker>::asLoadable)
      .toObservable()
      .startWithItem(LoadingFirst)
      .onErrorReturn(::FailedFirst)
}
