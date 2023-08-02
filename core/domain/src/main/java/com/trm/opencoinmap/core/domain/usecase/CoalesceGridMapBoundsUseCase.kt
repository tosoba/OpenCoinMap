package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.util.MapBoundsLimit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class CoalesceGridMapBoundsUseCase @Inject constructor() {
  operator fun invoke(gridMapBounds: GridMapBounds): GridMapBounds {
    val (minLat, maxLat, minLon, maxLon) = gridMapBounds.bounds
    return GridMapBounds(
      bounds =
        MapBounds(
          latSouth = max(minLat, MapBoundsLimit.MIN_LAT),
          latNorth = min(maxLat, MapBoundsLimit.MAX_LAT),
          lonWest = max(minLon, MapBoundsLimit.MIN_LON),
          lonEast = min(maxLon, MapBoundsLimit.MAX_LON)
        ),
      latDivisor = gridMapBounds.latDivisor,
      lonDivisor = gridMapBounds.lonDivisor
    )
  }
}
