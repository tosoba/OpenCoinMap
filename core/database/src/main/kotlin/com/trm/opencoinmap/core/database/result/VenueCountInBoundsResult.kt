package com.trm.opencoinmap.core.database.result

data class VenueCountInBoundsResult(
  val count: Int,
  val minLat: Double,
  val maxLat: Double,
  val minLon: Double,
  val maxLon: Double,
)
