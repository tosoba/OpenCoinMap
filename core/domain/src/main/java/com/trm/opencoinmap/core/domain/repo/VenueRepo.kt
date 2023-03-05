package com.trm.opencoinmap.core.domain.repo

import com.trm.opencoinmap.core.domain.model.MapMarker
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface VenueRepo {
  fun sync(): Completable

  fun getVenueMarkersInLatLngBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    latDivisor: Int,
    lonDivisor: Int,
  ): Single<List<MapMarker>>
}
