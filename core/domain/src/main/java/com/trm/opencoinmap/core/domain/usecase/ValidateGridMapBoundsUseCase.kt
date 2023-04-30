package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.util.MapBoundsLimit
import javax.inject.Inject

class ValidateGridMapBoundsUseCase @Inject constructor() {
  operator fun invoke(gridMapBounds: GridMapBounds): Boolean {
    val (minLat, maxLat, minLon, maxLon) = gridMapBounds.bounds
    return minLat >= MapBoundsLimit.MIN_LAT &&
      maxLat <= MapBoundsLimit.MAX_LAT &&
      minLon >= MapBoundsLimit.MIN_LON &&
      maxLon <= MapBoundsLimit.MAX_LON &&
      minLat < maxLat &&
      minLon < maxLon
  }
}
