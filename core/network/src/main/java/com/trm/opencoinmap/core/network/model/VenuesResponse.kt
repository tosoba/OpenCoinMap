package com.trm.opencoinmap.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class VenuesResponse(@SerialName("venues") val venues: List<Venue>?)
