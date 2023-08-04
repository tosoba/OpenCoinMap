package com.trm.opencoinmap.core.domain.model

data class GridMapBounds(
  val bounds: MapBounds,
  val latDivisor: Int,
  val lonDivisor: Int
)
