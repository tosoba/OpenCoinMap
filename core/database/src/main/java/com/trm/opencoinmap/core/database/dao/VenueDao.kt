package com.trm.opencoinmap.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trm.opencoinmap.core.database.entity.VenueEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

@Dao
interface VenueDao {
  @Upsert fun upsertCompletable(entities: List<VenueEntity>): Completable

  @Upsert fun upsert(entities: List<VenueEntity>)

  @Query(
    "SELECT * FROM venue " +
      "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon " +
      "AND EXISTS (" +
      "SELECT * FROM bounds WHERE whole = TRUE " +
      "UNION " +
      "SELECT * FROM bounds WHERE min_lat <= :minLat AND max_lat >= :maxLat AND min_lon <= :minLon AND max_lon >= :maxLon" +
      ")"
  )
  fun selectInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Single<List<VenueEntity>>
}
