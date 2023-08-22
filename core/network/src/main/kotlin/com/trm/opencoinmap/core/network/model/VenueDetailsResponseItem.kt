package com.trm.opencoinmap.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VenueDetailsResponseItem(
  @SerialName("category") val category: String?,
  @SerialName("city") val city: String?,
  @SerialName("coins") val coins: List<String>?,
  @SerialName("country") val country: String?,
  @SerialName("created_on") val createdOn: Int?,
  @SerialName("description") val description: String?,
  @SerialName("email") val email: String?,
  @SerialName("facebook") val facebook: String?,
  @SerialName("fax") val fax: String?,
  @SerialName("geolocation_degrees") val geolocationDegrees: String?,
  @SerialName("houseno") val houseNumber: String?,
  @SerialName("id") val id: Int?,
  @SerialName("instagram") val instagram: String?,
  @SerialName("lat") val lat: Double?,
  @SerialName("logo_url") val logoUrl: String?,
  @SerialName("lon") val lon: Double?,
  @SerialName("name") val name: String?,
  @SerialName("name_ascii") val nameAscii: String?,
  @SerialName("phone") val phone: String?,
  @SerialName("postcode") val postcode: String?,
  @SerialName("src_id") val srcId: String?,
  @SerialName("state") val state: String?,
  @SerialName("street") val street: String?,
  @SerialName("twitter") val twitter: String?,
  @SerialName("updated_on") val updatedOn: Int?,
  @SerialName("website") val website: String?
)
