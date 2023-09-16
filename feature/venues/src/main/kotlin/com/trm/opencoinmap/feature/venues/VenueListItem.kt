package com.trm.opencoinmap.feature.venues

import com.trm.opencoinmap.core.domain.model.Venue

data class VenueListItem(val venue: Venue, val distanceMeters: Double?)
