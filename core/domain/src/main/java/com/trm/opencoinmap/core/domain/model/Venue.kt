package com.trm.opencoinmap.core.domain.model

data class Venue(
  val id: Long,
  val lat: Double,
  val lon: Double,
  val category: String,
  val name: String,
  val createdOn: Long,
)
