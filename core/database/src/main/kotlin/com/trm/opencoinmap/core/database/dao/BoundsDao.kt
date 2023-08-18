package com.trm.opencoinmap.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trm.opencoinmap.core.database.entity.BoundsEntity
import io.reactivex.rxjava3.core.Single

@Dao
interface BoundsDao {
  @Upsert fun upsert(entity: BoundsEntity)

  @Query("DELETE FROM bounds WHERE whole = FALSE") fun deleteNonWhole()

  @Query("SELECT EXISTS (SELECT * FROM bounds WHERE whole = TRUE)")
  fun selectExistsWhole(): Single<Boolean>

  @Query(
    "SELECT EXISTS (" +
      "SELECT * FROM bounds WHERE whole = TRUE " +
      "UNION " +
      "SELECT * FROM bounds WHERE min_lat <= :minLat AND max_lat >= :maxLat AND min_lon <= :minLon AND max_lon >= :maxLon" +
      ")"
  )
  fun selectExistsBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Single<Boolean>
}
