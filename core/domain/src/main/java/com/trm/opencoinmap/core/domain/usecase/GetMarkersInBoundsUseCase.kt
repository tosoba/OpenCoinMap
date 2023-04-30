package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetMarkersInBoundsUseCase
@Inject
constructor(
  private val repo: VenueRepo,
  private val sendMarkersLoadingStatusUseCase: SendMarkersLoadingStatusUseCase
) {
  operator fun invoke(bounds: GridMapBounds): Observable<Loadable<List<MapMarker>>> =
    repo
      .getVenueMarkersInLatLngBounds(bounds)
      .map(List<MapMarker>::asLoadable)
      .toObservable()
      .startWithItem(LoadingFirst)
      .onErrorReturn(::FailedFirst)
      .doOnNext {
        sendMarkersLoadingStatusUseCase(
          status =
            when (it) {
              is Loading -> MarkersLoadingStatus.IN_PROGRESS
              is Ready -> MarkersLoadingStatus.SUCCESS
              is Failed -> MarkersLoadingStatus.ERROR
              else -> return@doOnNext
            }
        )
      }
}
