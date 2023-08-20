package com.trm.opencoinmap.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class VenueResponse(@SerialName("venue") val venue: Venue?)
