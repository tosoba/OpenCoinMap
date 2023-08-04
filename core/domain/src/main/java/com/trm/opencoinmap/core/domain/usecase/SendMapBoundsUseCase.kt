package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.MapBounds

fun interface SendMapBoundsUseCase {
  operator fun invoke(bounds: List<MapBounds>)
}
