package com.trm.opencoinmap.core.data.mapper

import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.model.VenueDetails
import com.trm.opencoinmap.core.network.model.VenueDetailsResponseItem
import com.trm.opencoinmap.core.network.model.VenueResponseItem

fun VenueResponseItem.asEntity(): VenueEntity =
  VenueEntity(
    id = requireNotNull(id),
    lat = requireNotNull(lat),
    lon = requireNotNull(lon),
    category = requireNotNull(category).trim(),
    name = requireNotNull(name).trim(),
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

fun VenueDetailsResponseItem.asDomainModel(): VenueDetails =
  VenueDetails(
    category = category,
    city = city,
    coins = coins,
    country = country,
    createdOn = createdOn,
    description = description,
    email = email,
    facebook = facebook,
    fax = fax,
    geolocationDegrees = geolocationDegrees,
    houseNumber = houseNumber,
    id = id,
    instagram = instagram,
    lat = lat,
    logoUrl = logoUrl,
    lon = lon,
    name = name,
    nameAscii = nameAscii,
    phone = phone,
    postcode = postcode,
    srcId = srcId,
    state = state,
    street = street,
    twitter = twitter,
    updatedOn = updatedOn,
    website = website
  )
