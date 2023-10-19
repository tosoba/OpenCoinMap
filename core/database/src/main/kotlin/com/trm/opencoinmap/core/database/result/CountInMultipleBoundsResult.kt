package com.trm.opencoinmap.core.database.result

data class CountInMultipleBoundsResult(
  val count: Int,
  val minLat: Double,
  val maxLat: Double,
  val minLon: Double,
  val maxLon: Double,
)
