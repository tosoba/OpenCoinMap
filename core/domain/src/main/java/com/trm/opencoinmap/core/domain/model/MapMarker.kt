package com.trm.opencoinmap.core.domain.model

sealed interface MapMarker {
  data class SingleVenue(val venue: Venue) : MapMarker
  data class VenuesCluster(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
    val size: Int
  ) : MapMarker {
    val lat: Double
      get() = (minLat + maxLat) / 2.0

    val lon: Double
      get() = (minLon + maxLon) / 2.0
  }
}
