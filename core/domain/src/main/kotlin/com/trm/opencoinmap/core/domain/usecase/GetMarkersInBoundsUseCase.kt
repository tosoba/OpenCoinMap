package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class GetMarkersInBoundsUseCase
@Inject
constructor(
  private val repo: VenueRepo,
  private val sendMarkersLoadingStatusUseCase: SendMarkersLoadingStatusUseCase
) {
  operator fun invoke(
    bounds: List<GridMapBounds>,
    query: String,
    categories: List<String>,
  ): Flowable<Loadable<List<MapMarker>>> =
    Flowable.zip(
        bounds.map {
          repo.getVenueMarkersInLatLngBounds(
            gridMapBounds = it,
            query = query,
            categories = categories
          )
        }
      ) { result ->
        result.filterIsInstance<List<MapMarker>>().flatten()
      }
      .map(List<MapMarker>::asLoadable)
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
