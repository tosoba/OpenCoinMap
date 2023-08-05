package com.trm.opencoinmap.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bounds")
data class BoundsEntity(
  @PrimaryKey(autoGenerate = true) var id: Long = 0L,
  @ColumnInfo("min_lat") val minLat: Double,
  @ColumnInfo("max_lat") val maxLat: Double,
  @ColumnInfo("min_lon") val minLon: Double,
  @ColumnInfo("max_lon") val maxLon: Double,
  val whole: Boolean,
)
