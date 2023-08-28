package com.trm.opencoinmap.core.data.ext

import com.trm.opencoinmap.core.network.model.VenueDetailsResponseItem
import com.trm.opencoinmap.core.network.model.VenueResponseItem

fun VenueResponseItem.isValid(): Boolean =
  id != null && lat != null && lon != null && category != null && name != null && createdOn != null

fun VenueDetailsResponseItem.isValid(): Boolean = id != null
