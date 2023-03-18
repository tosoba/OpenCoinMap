package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.util.BoundsConstants
import javax.inject.Inject

class ValidateGridMapBoundsUseCase @Inject constructor() {
  operator fun invoke(bounds: GridMapBounds): Boolean {
    val (minLat, maxLat, minLon, maxLon) = bounds
    return minLat >= BoundsConstants.MIN_LAT &&
      maxLat <= BoundsConstants.MAX_LAT &&
      minLon >= BoundsConstants.MIN_LON &&
      maxLon <= BoundsConstants.MAX_LON &&
      minLat < maxLat &&
      minLon < maxLon
  }
}
