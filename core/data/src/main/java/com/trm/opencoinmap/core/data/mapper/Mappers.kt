package com.trm.opencoinmap.core.data.mapper

import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.network.model.VenueResponseItem

fun VenueResponseItem.asEntity(): VenueEntity =
  VenueEntity(
    id = requireNotNull(id),
    lat = requireNotNull(lat),
    lon = requireNotNull(lon),
    category = requireNotNull(category),
    name = requireNotNull(name),
    createdOn = requireNotNull(createdOn)
  )

fun VenueEntity.asDomainModel(): Venue =
  Venue(id = id, lat = lat, lon = lon, category = category, name = name, createdOn = createdOn)

fun Venue.asEntity(): VenueEntity =
  VenueEntity(
    id = id,
    lat = lat,
    lon = lon,
    category = category,
    name = name,
    createdOn = createdOn
  )

fun VenueResponseItem.asDomainModel(): Venue =
  Venue(
    id = requireNotNull(id),
    lat = requireNotNull(lat),
    lon = requireNotNull(lon),
    category = requireNotNull(category),
    name = requireNotNull(name),
    createdOn = requireNotNull(createdOn)
  )
