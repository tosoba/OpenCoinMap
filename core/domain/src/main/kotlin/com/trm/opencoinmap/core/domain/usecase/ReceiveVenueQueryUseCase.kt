package com.trm.opencoinmap.core.domain.usecase

import io.reactivex.rxjava3.core.Observable

fun interface ReceiveVenueQueryUseCase {
  operator fun invoke(): Observable<String>
}
