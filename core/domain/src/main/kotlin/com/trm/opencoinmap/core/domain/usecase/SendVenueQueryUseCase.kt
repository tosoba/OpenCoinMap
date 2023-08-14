package com.trm.opencoinmap.core.domain.usecase

fun interface SendVenueQueryUseCase {
  operator fun invoke(query: String)
}
