package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.MarkersLoadingStatus

fun interface SendMarkersLoadingStatusUseCase {
  operator fun invoke(status: MarkersLoadingStatus)
}
