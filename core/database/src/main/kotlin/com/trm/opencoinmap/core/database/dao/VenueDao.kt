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

  @Query(SELECT_MATCHING_QUERY_IN_EXISTING_BOUNDS)
  fun selectMatchingQueryInBoundsSingle(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String
  ): Single<List<VenueEntity>>

  @Query(COUNT_MATCHING_QUERY_IN_BOUNDS)
  fun countMatchingQueryInBoundsSingle(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String
  ): Single<Int>

  @Query(SELECT_MATCHING_QUERY_IN_EXISTING_BOUNDS)
  fun selectMatchingQueryInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String
  ): List<VenueEntity>

  @Query(SELECT_MATCHING_QUERY_IN_BOUNDS)
  fun selectPageInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String
  ): PagingSource<Int, VenueEntity>

  @Query(
    "SELECT * FROM venue " +
      "WHERE (lat >= :minLat1 AND lat <= :maxLat1 AND lon >= :minLon1 AND lon <= :maxLon1 AND (:query = '' OR LOWER(name) LIKE :query)) " +
      "OR (lat >= :minLat2 AND lat <= :maxLat2 AND lon >= :minLon2 AND lon <= :maxLon2 AND (:query = '' OR LOWER(name) LIKE :query))"
  )
  fun selectPageIn2Bounds(
    minLat1: Double,
    maxLat1: Double,
    minLon1: Double,
    maxLon1: Double,
    minLat2: Double,
    maxLat2: Double,
    minLon2: Double,
    maxLon2: Double,
    query: String
  ): PagingSource<Int, VenueEntity>

  @Query(COUNT_MATCHING_QUERY_IN_BOUNDS)
  fun countMatchingQueryInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String,
  ): Int

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

  @Query("SELECT category, COUNT(*) AS count FROM venue GROUP BY category ORDER BY category")
  fun selectDistinctCategories(): Flowable<List<VenueCategoryCountResult>>

  companion object {
    private const val SELECT_MATCHING_QUERY_IN_BOUNDS =
      "SELECT * FROM venue " +
          "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%') " +
          "ORDER BY CASE WHEN LOWER(name) LIKE LOWER(:query) || '%' THEN 1 ELSE 2 END, LOWER(name)"

    private const val SELECT_MATCHING_QUERY_IN_EXISTING_BOUNDS =
      "SELECT * FROM venue " +
          "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%') " +
          "AND EXISTS (" +
          "SELECT * FROM bounds WHERE whole = TRUE " +
          "UNION " +
          "SELECT * FROM bounds WHERE min_lat <= :minLat AND max_lat >= :maxLat AND min_lon <= :minLon AND max_lon >= :maxLon" +
          ")"

    private const val COUNT_MATCHING_QUERY_IN_BOUNDS =
      "SELECT COUNT(*) FROM venue " +
          "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%')"
  }
}
