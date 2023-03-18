package com.trm.opencoinmap.core.domain.model

data class GridMapBounds(
  val minLat: Double,
  val maxLat: Double,
  val minLon: Double,
  val maxLon: Double,
  val latDivisor: Int,
  val lonDivisor: Int
)
