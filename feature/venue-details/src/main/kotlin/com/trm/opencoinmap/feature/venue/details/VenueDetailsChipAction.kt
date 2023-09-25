package com.trm.opencoinmap.feature.venue.details

import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class VenueDetailsChipAction(
  @StringRes val textRes: Int,
  @DrawableRes val drawableRes: Int,
  val onClick: View.OnClickListener
)
