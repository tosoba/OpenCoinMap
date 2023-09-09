package com.trm.opencoinmap.feature.venues

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.trm.opencoinmap.core.common.R as commonR

sealed interface VenuesInfoState {
  object Hidden : VenuesInfoState
  object Error : VenuesInfoState
  object Empty : VenuesInfoState

  val isVisible: Boolean
    get() = this !is Hidden

  val drawableRes: Int
    @DrawableRes get() = if (this is Empty) commonR.drawable.no_results else commonR.drawable.error

  val textRes: Int
    @StringRes
    get() = if (this is Empty) R.string.no_venues_found else R.string.venues_error_occurred
}
