package com.trm.opencoinmap.core.domain.usecase

fun interface SendVenueClickedEventUseCase {
  operator fun invoke(id: Long)
}
