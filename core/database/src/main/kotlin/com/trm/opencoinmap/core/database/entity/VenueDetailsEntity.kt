package com.trm.opencoinmap.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venue_details")
data class VenueDetailsEntity(
  val category: String?,
  val city: String?,
  val coins: List<String>?,
  val country: String?,
  val createdOn: Int?,
  val description: String?,
  val email: String?,
  val facebook: String?,
  val fax: String?,
  val geolocationDegrees: String?,
  val houseNumber: String?,
  @PrimaryKey val id: Long,
  val instagram: String?,
  val lat: Double?,
  val logoUrl: String?,
  val lon: Double?,
  val name: String?,
  val nameAscii: String?,
  val phone: String?,
  val postcode: String?,
  val srcId: String?,
  val state: String?,
  val street: String?,
  val twitter: String?,
  val updatedOn: Int?,
  val website: String?
)
