package com.trm.opencoinmap.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("venue")
data class VenueEntity(
  @PrimaryKey val id: Int,
  val lat: Double,
  val lon: Double,
  @ColumnInfo("category") val category: String,
  val name: String,
  @ColumnInfo("created_on") val createdOn: Long,
)
