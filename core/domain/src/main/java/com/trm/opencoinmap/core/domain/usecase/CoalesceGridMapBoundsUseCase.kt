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
          minLat = max(minLat, MapBoundsLimit.MIN_LAT),
          maxLat = min(maxLat, MapBoundsLimit.MAX_LAT),
          minLon = max(minLon, MapBoundsLimit.MIN_LON),
          maxLon = min(maxLon, MapBoundsLimit.MAX_LON)
        ),
      latDivisor = gridMapBounds.latDivisor,
      lonDivisor = gridMapBounds.lonDivisor
    )
  }
}
