package com.trm.opencoinmap.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Venue(
  @SerialName("category") val category: String?,
  @SerialName("created_on") val createdOn: Long?,
  @SerialName("id") val id: Int?,
  @SerialName("lat") val lat: Double?,
  @SerialName("lon") val lon: Double?,
  @SerialName("name") val name: String?,
)
