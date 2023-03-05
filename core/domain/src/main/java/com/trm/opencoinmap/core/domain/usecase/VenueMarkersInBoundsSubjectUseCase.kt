package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.VenueMarkersInBounds
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.domain.util.BoundsConstants
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VenueMarkersInBoundsSubjectUseCase @Inject constructor(private val repo: VenueRepo) {
  private val loadSubject = PublishSubject.create<Args>()

  fun onNext(args: Args) {
    val (minLat, maxLat, minLon, maxLon) = args
    if (
      minLat < BoundsConstants.MIN_LAT ||
        maxLat > BoundsConstants.MAX_LAT ||
        minLon < BoundsConstants.MIN_LON ||
        maxLon > BoundsConstants.MAX_LON ||
        minLat >= maxLat ||
        minLon >= maxLon
    ) {
      throw IllegalArgumentException("Invalid bounds.")
    }
    loadSubject.onNext(args)
  }

  fun observable(): Observable<VenueMarkersInBounds> =
    loadSubject.distinctUntilChanged().debounce(1L, TimeUnit.SECONDS).switchMapSingle { args ->
      val (minLat, maxLat, minLon, maxLon, latDivisor, lonDivisor) = args
      repo.getVenueMarkersInLatLngBounds(
        minLat = minLat,
        maxLat = maxLat,
        minLon = minLon,
        maxLon = maxLon,
        latDivisor = latDivisor,
        lonDivisor = lonDivisor,
      )
    }

  data class Args(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
    val latDivisor: Int,
    val lonDivisor: Int
  )
}
