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
  @Query("SELECT COUNT(*) FROM venue") fun selectCount(): Single<Int>

  @Upsert fun upsert(entities: List<VenueEntity>)

  @Query(SELECT_MATCHING_QUERY_IN_EXISTING_BOUNDS)
  fun selectMatchingQueryInBoundsSingle(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String,
    categories: List<String>,
    categoriesCount: Int
  ): Single<List<VenueEntity>>

  @Query(COUNT_MATCHING_QUERY_IN_BOUNDS)
  fun countMatchingQueryInBoundsSingle(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String,
    categories: List<String>,
    categoriesCount: Int
  ): Single<Int>

  @Query(SELECT_MATCHING_QUERY_IN_EXISTING_BOUNDS)
  fun selectMatchingQueryInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String,
    categories: List<String>,
    categoriesCount: Int
  ): List<VenueEntity>

  @Query(SELECT_MATCHING_QUERY_IN_BOUNDS)
  fun selectPageInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String,
    categories: List<String>,
    categoriesCount: Int
  ): PagingSource<Int, VenueEntity>

  @Query(
    "SELECT * FROM venue " +
      "WHERE ((lat >= :minLat1 AND lat <= :maxLat1 AND lon >= :minLon1 AND lon <= :maxLon1) " +
      "OR (lat >= :minLat2 AND lat <= :maxLat2 AND lon >= :minLon2 AND lon <= :maxLon2)) " +
      "AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%') " +
      "AND (:categoriesCount = 0 OR category IN (:categories)) " +
      "ORDER BY CASE WHEN LOWER(name) LIKE LOWER(:query) || '%' THEN 1 ELSE 2 END, LOWER(name)"
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
    query: String,
    categories: List<String>,
    categoriesCount: Int
  ): PagingSource<Int, VenueEntity>

  @Query(COUNT_MATCHING_QUERY_IN_BOUNDS)
  fun countMatchingQueryInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String,
    categories: List<String>,
    categoriesCount: Int
  ): Int

  @Query(
    "SELECT * FROM " +
      "(SELECT category, COUNT(*) AS count FROM venue " +
      "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon " +
      "AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%') " +
      "GROUP BY category " +
      "UNION " +
      "SELECT category, 0 AS count FROM venue " +
      "WHERE category NOT IN " +
      "(SELECT category FROM venue " +
      "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon " +
      "AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%')" +
      ")" +
      ") " +
      "ORDER BY category"
  )
  fun selectCategoriesWithCountInBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String
  ): Flowable<List<VenueCategoryCountResult>>

  @Query(
    "SELECT * FROM " +
      "(SELECT category, COUNT(*) AS count FROM venue " +
      "WHERE ((lat >= :minLat1 AND lat <= :maxLat1 AND lon >= :minLon1 AND lon <= :maxLon1) " +
      "OR (lat >= :minLat2 AND lat <= :maxLat2 AND lon >= :minLon2 AND lon <= :maxLon2)) " +
      "AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%') " +
      "GROUP BY category " +
      "UNION " +
      "SELECT category, 0 AS count FROM venue " +
      "WHERE category NOT IN " +
      "(SELECT category FROM venue " +
      "WHERE ((lat >= :minLat1 AND lat <= :maxLat1 AND lon >= :minLon1 AND lon <= :maxLon1) " +
      "OR (lat >= :minLat2 AND lat <= :maxLat2 AND lon >= :minLon2 AND lon <= :maxLon2)) " +
      "AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%')" +
      ")" +
      ") " +
      "ORDER BY category"
  )
  fun selectCategoriesWithCountIn2Bounds(
    minLat1: Double,
    maxLat1: Double,
    minLon1: Double,
    maxLon1: Double,
    minLat2: Double,
    maxLat2: Double,
    minLon2: Double,
    maxLon2: Double,
    query: String
  ): Flowable<List<VenueCategoryCountResult>>

  companion object {
    private const val SELECT_MATCHING_QUERY_IN_BOUNDS =
      "SELECT * FROM venue " +
        "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon " +
        "AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%') " +
        "AND (:categoriesCount = 0 OR category IN (:categories)) " +
        "ORDER BY CASE WHEN LOWER(name) LIKE LOWER(:query) || '%' THEN 1 ELSE 2 END, LOWER(name)"

    private const val SELECT_MATCHING_QUERY_IN_EXISTING_BOUNDS =
      "SELECT * FROM venue " +
        "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon " +
        "AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%') " +
        "AND (:categoriesCount = 0 OR category IN (:categories)) " +
        "AND EXISTS (" +
        "SELECT * FROM bounds WHERE whole = TRUE " +
        "UNION " +
        "SELECT * FROM bounds WHERE min_lat <= :minLat AND max_lat >= :maxLat AND min_lon <= :minLon AND max_lon >= :maxLon" +
        ")"

    private const val COUNT_MATCHING_QUERY_IN_BOUNDS =
      "SELECT COUNT(*) FROM venue " +
        "WHERE lat >= :minLat AND lat <= :maxLat AND lon >= :minLon AND lon <= :maxLon " +
        "AND (:query = '' OR LOWER(name) LIKE '%' || LOWER(:query) || '%') " +
        "AND (:categoriesCount = 0 OR category IN (:categories))"
  }
}
