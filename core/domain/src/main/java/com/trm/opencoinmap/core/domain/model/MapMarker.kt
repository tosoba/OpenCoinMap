package com.trm.opencoinmap.core.domain.model

sealed interface MapMarker {
  data class SingleVenue(val venue: Venue) : MapMarker
  data class VenuesCluster(
    val latSouth: Double,
    val latNorth: Double,
    val lonEast: Double,
    val lonWest: Double,
    val size: Int
  ) : MapMarker {
    val lat: Double
      get() = (latSouth + latNorth) / 2.0

    val lon: Double
      get() = (lonEast + lonWest) / 2.0
  }
}
