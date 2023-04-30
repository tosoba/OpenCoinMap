package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.MarkersLoadingStatus
import io.reactivex.rxjava3.core.Observable

fun interface ReceiveMarkersLoadingStatusUseCase {
  operator fun invoke(): Observable<MarkersLoadingStatus>
}
