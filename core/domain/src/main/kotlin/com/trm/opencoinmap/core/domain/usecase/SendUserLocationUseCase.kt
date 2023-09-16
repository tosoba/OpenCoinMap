package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.LatLng

fun interface SendUserLocationUseCase {
  operator fun invoke(location: LatLng)
}
