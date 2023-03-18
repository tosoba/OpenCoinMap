package com.trm.opencoinmap.core.domain.model

sealed interface MapMarker {
  data class SingleVenue(val venue: Venue) : MapMarker
  data class VenuesCluster(val lat: Double, val lon: Double, val size: Int) : MapMarker
}
