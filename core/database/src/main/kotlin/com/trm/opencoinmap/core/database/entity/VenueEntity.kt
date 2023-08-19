package com.trm.opencoinmap.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "venue", indices = [Index("lat", "lon", "name", "category")])
data class VenueEntity(
  @PrimaryKey val id: Long,
  val lat: Double,
  val lon: Double,
  val category: String,
  val name: String,
  @ColumnInfo("created_on") val createdOn: Long,
)
