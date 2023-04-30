package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.MapBounds
import io.reactivex.rxjava3.core.Observable

fun interface ReceiveMapBoundsUseCase {
  operator fun invoke(): Observable<MapBounds>
}
