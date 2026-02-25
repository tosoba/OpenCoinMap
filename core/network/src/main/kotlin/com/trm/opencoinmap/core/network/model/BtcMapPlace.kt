package com.trm.opencoinmap.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BtcMapPlace(
  @SerialName("id") val id: Long,
  @SerialName("lat") val lat: Double,
  @SerialName("lon") val lon: Double,
  @SerialName("icon") val icon: String? = null,
  @SerialName("name") val name: String? = null,
  @SerialName("address") val address: String? = null,
  @SerialName("opening_hours") val openingHours: String? = null,
  @SerialName("created_at") val createdAt: String? = null,
  @SerialName("updated_at") val updatedAt: String? = null,
  @SerialName("verified_at") val verifiedAt: String? = null,
  @SerialName("osm_id") val osmId: String? = null,
  @SerialName("phone") val phone: String? = null,
  @SerialName("website") val website: String? = null,
  @SerialName("email") val email: String? = null,
  @SerialName("facebook") val facebook: String? = null,
  @SerialName("instagram") val instagram: String? = null,
  @SerialName("twitter") val twitter: String? = null,
  @SerialName("description") val description: String? = null,
  @SerialName("image") val image: String? = null,
)
