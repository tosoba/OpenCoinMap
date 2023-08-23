package com.trm.opencoinmap.feature.venue.details

import android.os.Bundle
import androidx.core.os.bundleOf

object VenueDetailsArgs {
  internal const val VENUE_ID_KEY = "venueId"
  internal const val VENUE_NAME_KEY = "venueName"

  fun argsBundle(id: Long, name: String): Bundle =
    bundleOf(VENUE_ID_KEY to id, VENUE_NAME_KEY to name)
}

fun Bundle.getVenueName(): String? = getString(VenueDetailsArgs.VENUE_NAME_KEY)
