package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.Venue
import io.reactivex.rxjava3.core.Observable

fun interface ReceiveVenueClickedEventUseCase {
  operator fun invoke(): Observable<Venue>
}
