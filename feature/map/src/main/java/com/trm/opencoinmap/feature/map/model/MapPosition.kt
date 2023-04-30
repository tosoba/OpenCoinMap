package com.trm.opencoinmap.feature.map.model

import android.os.Parcelable
import com.trm.opencoinmap.feature.map.util.MapDefaults
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MapPosition(
  val latitude: Double = MapDefaults.LATITUDE,
  val longitude: Double = MapDefaults.LONGITUDE,
  val zoom: Double = MapDefaults.MIN_ZOOM,
  val orientation: Float = MapDefaults.ORIENTATION
) : Parcelable
