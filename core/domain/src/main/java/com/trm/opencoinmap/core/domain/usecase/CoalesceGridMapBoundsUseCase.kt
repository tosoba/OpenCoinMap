package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.util.MapBoundsLimit
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class CoalesceGridMapBoundsUseCase @Inject constructor() {
  operator fun invoke(gridMapBounds: GridMapBounds, centerLon: Double): List<GridMapBounds> {
    val (bounds, latDivisor, lonDivisor) = gridMapBounds
    val (latSouth, latNorth, lonWest, lonEast) = bounds

    fun gridMapBounds(lonWest: Double, lonEast: Double): GridMapBounds =
      GridMapBounds(
        bounds =
          MapBounds(
            latSouth = max(latSouth, MapBoundsLimit.MIN_LAT),
            latNorth = min(latNorth, MapBoundsLimit.MAX_LAT),
            lonWest = lonWest,
            lonEast = lonEast
          ),
        latDivisor = latDivisor,
        lonDivisor = lonDivisor
      )

    return buildList {
      if (lonWest > centerLon || centerLon > lonEast) {
        add(gridMapBounds(lonWest = lonWest, lonEast = MapBoundsLimit.MAX_LON))
        add(gridMapBounds(lonWest = MapBoundsLimit.MIN_LON, lonEast = lonEast))
      } else {
        add(
          gridMapBounds(
            lonWest = max(lonWest, MapBoundsLimit.MIN_LON),
            lonEast = min(lonEast, MapBoundsLimit.MAX_LON)
          )
        )
      }
    }
  }
}
