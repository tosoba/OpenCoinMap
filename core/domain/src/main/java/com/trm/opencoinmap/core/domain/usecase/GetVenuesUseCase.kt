package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetVenuesUseCase @Inject constructor(private val repo: VenueRepo) {
  private val loadSubject = PublishSubject.create<Args>()

  operator fun invoke(args: Args) {
    val (minLat, maxLat, minLon, maxLon) = args
    if (
      minLat < -90.0 ||
        maxLat > 90.0 ||
        minLon < -180.0 ||
        maxLon > 180.0 ||
        minLat >= maxLat ||
        minLon >= maxLon
    ) {
      throw IllegalArgumentException("Invalid bounds.")
    }
    loadSubject.onNext(args)
  }

  operator fun invoke(): Observable<List<Venue>> =
    loadSubject.debounce(1L, TimeUnit.SECONDS).switchMapSingle { args ->
      val (minLat, maxLat, minLon, maxLon) = args
      repo.getVenuesInLatLngBounds(
        minLat = minLat,
        maxLat = maxLat,
        minLon = minLon,
        maxLon = maxLon
      )
    }

  data class Args(val minLat: Double, val maxLat: Double, val minLon: Double, val maxLon: Double)
}
