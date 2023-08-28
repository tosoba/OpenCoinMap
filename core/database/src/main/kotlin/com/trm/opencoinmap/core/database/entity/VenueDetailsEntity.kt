package com.trm.opencoinmap.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venue_details")
data class VenueDetailsEntity(
  val category: String?,
  val city: String?,
  val coins: List<String>?,
  val country: String?,
  @ColumnInfo("created_on") val createdOn: Int?,
  val description: String?,
  val email: String?,
  val facebook: String?,
  val fax: String?,
  @ColumnInfo("geolocation_degrees") val geolocationDegrees: String?,
  @ColumnInfo("house_number") val houseNumber: String?,
  @PrimaryKey val id: Long,
  val instagram: String?,
  val lat: Double?,
  @ColumnInfo("logo_url") val logoUrl: String?,
  val lon: Double?,
  val name: String?,
  @ColumnInfo("name_ascii") val nameAscii: String?,
  val phone: String?,
  val postcode: String?,
  @ColumnInfo("src_id") val srcId: String?,
  val state: String?,
  val street: String?,
  val twitter: String?,
  @ColumnInfo("updated_on") val updatedOn: Int?,
  val website: String?,
  @ColumnInfo("inserted_at_timestamp") val insertedAtTimestamp: Long
)
