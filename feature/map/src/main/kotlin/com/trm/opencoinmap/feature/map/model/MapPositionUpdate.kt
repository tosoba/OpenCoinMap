package com.trm.opencoinmap.feature.map.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MapPositionUpdate(
  val position: MapPosition,
  val shouldUpdate: Boolean,
) : Parcelable
