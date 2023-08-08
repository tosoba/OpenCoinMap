package com.trm.opencoinmap.core.domain.usecase

import io.reactivex.rxjava3.core.Observable

fun interface ReceiveVenueClickedEventUseCase {
  operator fun invoke(): Observable<Long>
}
