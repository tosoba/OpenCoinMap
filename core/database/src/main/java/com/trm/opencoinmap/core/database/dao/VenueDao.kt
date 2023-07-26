package com.trm.opencoinmap.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trm.opencoinmap.core.database.entity.VenueCategoryCountResult
import com.trm.opencoinmap.core.database.entity.VenueEntity
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Dao
interface VenueDao {
  @Upsert fun upsert(entities: List<VenueEntity>)

  @Query(SELECT_IN_EXISTING_BOUNDS)
  fun selectInBoundsSingle(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Single<List<VenueEntity>>

  @Query(COUNT_IN_BOUNDS)
  fun countInBoundsSingle(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Single<Int>

  @Query(SELECT_IN_EXISTING_BOUNDS)
  fun selectInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): List<VenueEntity>

  @Query(SELECT_IN_BOUNDS)
  fun selectPageInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): PagingSource<Int, VenueEntity>

  @Query(COUNT_IN_BOUNDS)
  fun countInBounds(minLat: Double, maxLat: Double, minLon: Double, maxLon: Double): Int

  @Query(
    "SELECT EXISTS (" +
      "SELECT * FROM bounds WHERE whole = TRUE " +
      "UNION " +
      "SELECT * FROM bounds WHERE min_lat <= :minLat AND max_lat >= :maxLat AND min_lon <= :minLon AND max_lon >= :maxLon" +
      ")"
  )
  fun allExistInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Single<Boolean>

  @Query(
    "SELECT category, COUNT(*) AS count FROM venue " +
      "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon " +
      "GROUP BY category " +
      "ORDER BY category"
  )
  fun selectDistinctCategoriesInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Flowable<List<VenueCategoryCountResult>>

  companion object {
    private const val SELECT_IN_BOUNDS =
      "SELECT * FROM venue " +
        "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon"

    private const val SELECT_IN_EXISTING_BOUNDS =
      "SELECT * FROM venue " +
        "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon " +
        "AND EXISTS (" +
        "SELECT * FROM bounds WHERE whole = TRUE " +
        "UNION " +
        "SELECT * FROM bounds WHERE min_lat <= :minLat AND max_lat >= :maxLat AND min_lon <= :minLon AND max_lon >= :maxLon" +
        ")"

    private const val COUNT_IN_BOUNDS =
      "SELECT COUNT(*) FROM venue " +
        "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon"
  }
}
