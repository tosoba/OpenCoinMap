package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class GetMarkersInBoundsUseCase
@Inject
constructor(
  private val repo: VenueRepo,
  private val sendMarkersLoadingStatusUseCase: SendMarkersLoadingStatusUseCase
) {
  operator fun invoke(bounds: List<GridMapBounds>): Observable<Loadable<List<MapMarker>>> =
    Single.zip(bounds.map(repo::getVenueMarkersInLatLngBounds)) { result ->
        result.filterIsInstance<List<MapMarker>>().flatten()
      }
      .map(List<MapMarker>::asLoadable)
      .toObservable()
      .startWithItem(LoadingFirst)
      .onErrorReturn(::FailedFirst)
      .doOnNext {
        sendMarkersLoadingStatusUseCase(
          status =
            when (it) {
              is Loading -> MarkersLoadingStatus.InProgress
              is Ready -> MarkersLoadingStatus.Success
              is Failed -> MarkersLoadingStatus.Error(it.throwable)
              else -> return@doOnNext
            }
        )
      }
}
