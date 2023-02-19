package com.trm.opencoinmap.core.domain.repo

import com.trm.opencoinmap.core.domain.model.Venue
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface VenueRepo {
  fun sync(): Completable

  fun getVenuesInLatLngBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Single<List<Venue>>
}
