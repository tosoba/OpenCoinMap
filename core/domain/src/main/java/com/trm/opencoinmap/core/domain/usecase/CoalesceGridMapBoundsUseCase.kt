package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.util.MapBoundsLimit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class CoalesceGridMapBoundsUseCase @Inject constructor() {
  operator fun invoke(gridMapBounds: GridMapBounds): GridMapBounds {
    val (latSouth, latNorth, lonWest, lonEast) = gridMapBounds.bounds
    return GridMapBounds(
      bounds =
        MapBounds(
          latSouth = max(latSouth, MapBoundsLimit.MIN_LAT),
          latNorth = min(latNorth, MapBoundsLimit.MAX_LAT),
          lonWest = max(lonWest, MapBoundsLimit.MIN_LON),
          lonEast = min(lonEast, MapBoundsLimit.MAX_LON)
        ),
      latDivisor = gridMapBounds.latDivisor,
      lonDivisor = gridMapBounds.lonDivisor
    )
  }
}
