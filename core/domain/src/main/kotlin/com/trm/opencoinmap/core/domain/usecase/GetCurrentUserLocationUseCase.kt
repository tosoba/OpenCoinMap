package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.LatLng
import io.reactivex.rxjava3.core.Maybe

fun interface GetCurrentUserLocationUseCase {
  operator fun invoke(): Maybe<LatLng>
}
