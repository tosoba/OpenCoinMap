package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.LatLng
import io.reactivex.rxjava3.core.Observable

fun interface ReceiveUserLocationUseCase {
  operator fun invoke(): Observable<LatLng>
}
