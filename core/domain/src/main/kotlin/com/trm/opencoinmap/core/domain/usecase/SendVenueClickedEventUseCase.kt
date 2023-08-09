package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.Venue

fun interface SendVenueClickedEventUseCase {
  operator fun invoke(venue: Venue)
}
