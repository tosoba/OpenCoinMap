package com.trm.opencoinmap.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Venue(
  @SerialName("id") val id: Long?,
  @SerialName("lat") val lat: Double?,
  @SerialName("lon") val lon: Double?,
  @SerialName("category") val category: String?,
  @SerialName("name") val name: String?,
  @SerialName("created_on") val createdOn: Long?,
)
