package com.trm.opencoinmap.core.data.mapper

import com.trm.opencoinmap.core.database.entity.VenueDetailsEntity
import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.model.VenueDetails
import com.trm.opencoinmap.core.network.model.BtcMapPlace
import java.time.Instant

fun BtcMapPlace.asEntity(): VenueEntity =
  VenueEntity(
    id = id,
    lat = lat,
    lon = lon,
    category = icon ?: "unknown",
    name = name ?: "Unknown",
    createdOn = createdAt?.let { Instant.parse(it).toEpochMilli() } ?: 0L,
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
    createdOn = createdOn,
  )

fun BtcMapPlace.asDomainModel(): Venue =
  Venue(
    id = id,
    lat = lat,
    lon = lon,
    category = icon ?: "unknown",
    name = name ?: "Unknown",
    createdOn = createdAt?.let { Instant.parse(it).toEpochMilli() } ?: 0L,
  )

fun BtcMapPlace.asDetailsEntity(): VenueDetailsEntity =
  VenueDetailsEntity(
    category = icon,
    createdOn = createdAt?.let { Instant.parse(it).epochSecond.toInt() },
    description = description,
    email = email,
    facebook = facebook,
    id = id,
    instagram = instagram,
    lat = lat,
    logoUrl = image,
    lon = lon,
    name = name,
    nameAscii = name,
    phone = phone,
    srcId = osmId,
    street = address,
    twitter = twitter,
    updatedOn = updatedAt?.let { Instant.parse(it).epochSecond.toInt() },
    website = website,
    insertedAtTimestamp = System.currentTimeMillis(),
  )

fun VenueDetailsEntity.asDomainModel(): VenueDetails =
  VenueDetails(
    category = category,
    createdOn = createdOn,
    description = description,
    email = email,
    facebook = facebook,
    id = id,
    instagram = instagram,
    lat = lat,
    logoUrl = logoUrl,
    lon = lon,
    name = name,
    nameAscii = nameAscii,
    phone = phone,
    srcId = srcId,
    street = street,
    twitter = twitter,
    updatedOn = updatedOn,
    website = website,
  )
