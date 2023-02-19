package com.trm.opencoinmap.core.data.mapper

import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.network.model.Venue

fun Venue.asEntity(): VenueEntity =
  VenueEntity(
    id = requireNotNull(id),
    lat = requireNotNull(lat),
    lon = requireNotNull(lon),
    category = requireNotNull(category),
    name = requireNotNull(name),
    createdOn = requireNotNull(createdOn)
  )
