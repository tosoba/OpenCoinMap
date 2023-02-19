package com.trm.opencoinmap.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trm.opencoinmap.core.database.entity.VenueEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface VenueDao {
  @Upsert fun upsert(entities: List<VenueEntity>): Completable

  @Query(
    "SELECT * FROM venue WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon"
  )
  fun selectInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Single<List<VenueEntity>>

  @Query("SELECT EXISTS(SELECT * FROM venue LIMIT 1)") fun anyExists(): Single<Boolean>
}
