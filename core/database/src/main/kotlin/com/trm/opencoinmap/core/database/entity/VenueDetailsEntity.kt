package com.trm.opencoinmap.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venue_details")
data class VenueDetailsEntity(
  val category: String?,
  @ColumnInfo("created_on") val createdOn: Int?,
  val description: String?,
  val email: String?,
  val facebook: String?,
  @PrimaryKey val id: Long,
  val instagram: String?,
  val lat: Double?,
  @ColumnInfo("logo_url") val logoUrl: String?,
  val lon: Double?,
  val name: String?,
  @ColumnInfo("name_ascii") val nameAscii: String?,
  val phone: String?,
  @ColumnInfo("src_id") val srcId: String?,
  val street: String?,
  val twitter: String?,
  @ColumnInfo("updated_on") val updatedOn: Int?,
  val website: String?,
  @ColumnInfo("inserted_at_timestamp") val insertedAtTimestamp: Long,
)
