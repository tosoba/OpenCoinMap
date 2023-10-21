package com.trm.opencoinmap.core.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.rxjava3.flowable
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.trm.opencoinmap.core.data.ext.isValid
import com.trm.opencoinmap.core.data.mapper.asDomainModel
import com.trm.opencoinmap.core.data.mapper.asEntity
import com.trm.opencoinmap.core.database.OpenCoinMapDatabase
import com.trm.opencoinmap.core.database.entity.BoundsEntity
import com.trm.opencoinmap.core.database.entity.VenueDetailsEntity
import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.core.domain.model.VenueDetails
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.domain.sync.SyncDataSource
import com.trm.opencoinmap.core.domain.util.MapBoundsLimit
import com.trm.opencoinmap.core.network.model.VenueDetailsResponseItem
import com.trm.opencoinmap.core.network.model.VenueResponseItem
import com.trm.opencoinmap.core.network.retrofit.CoinMapApi
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.Callable
import javax.inject.Inject

class VenueRepoImpl
@Inject
constructor(
  private val coinMapApi: CoinMapApi,
  private val db: OpenCoinMapDatabase,
  private val syncDataSource: SyncDataSource
) : VenueRepo {
  private val venueDao = db.venueDao()
  private val boundsDao = db.boundsDao()
  private val venueDetailsDao = db.venueDetailsDao()

  override fun sync(): Completable =
    boundsDao
      .selectExistsWhole()
      .ignoreElement()
      .andThen(
        coinMapApi
          .getVenues()
          .map { response ->
            response.venues?.filter(VenueResponseItem::isValid)?.map(VenueResponseItem::asEntity)
              ?: emptyList()
          }
          .flatMapCompletable(::insertVenuesInWholeBounds)
      )

  override fun getVenuesPagingInBounds(
    mapBounds: List<MapBounds>,
    query: String,
    categories: List<String>
  ): Flowable<PagingData<Venue>> =
    Pager(config = PagingConfig(pageSize = 50, enablePlaceholders = false, initialLoadSize = 50)) {
        when (mapBounds.size) {
          1 -> {
            val (latSouth, latNorth, lonWest, lonEast) = mapBounds.first()
            venueDao.selectPageInBounds(
              minLat = latSouth,
              maxLat = latNorth,
              minLon = lonWest,
              maxLon = lonEast,
              query = query,
              categories = categories,
              categoriesCount = categories.size
            )
          }
          2 -> {
            val (latSouth1, latNorth1, lonWest1, lonEast1) = mapBounds.first()
            val (latSouth2, latNorth2, lonWest2, lonEast2) = mapBounds.last()
            venueDao.selectPageIn2Bounds(
              minLat1 = latSouth1,
              maxLat1 = latNorth1,
              minLon1 = lonWest1,
              maxLon1 = lonEast1,
              minLat2 = latSouth2,
              maxLat2 = latNorth2,
              minLon2 = lonWest2,
              maxLon2 = lonEast2,
              query = query,
              categories = categories,
              categoriesCount = categories.size
            )
          }
          else -> throw IllegalArgumentException("Invalid map bounds.")
        }
      }
      .flowable
      .map { it.map(VenueEntity::asDomainModel) }

  override fun getCategoriesWithCountInBounds(
    mapBounds: List<MapBounds>,
    query: String
  ): Flowable<List<VenueCategoryCount>> =
    Flowable.defer {
        when (mapBounds.size) {
          1 -> {
            val (latSouth, latNorth, lonWest, lonEast) = mapBounds.first()
            venueDao.selectCategoriesWithCountInBounds(
              minLat = latSouth,
              maxLat = latNorth,
              minLon = lonWest,
              maxLon = lonEast,
              query = query
            )
          }
          2 -> {
            val (latSouth1, latNorth1, lonWest1, lonEast1) = mapBounds.first()
            val (latSouth2, latNorth2, lonWest2, lonEast2) = mapBounds.last()
            venueDao.selectCategoriesWithCountIn2Bounds(
              minLat1 = latSouth1,
              maxLat1 = latNorth1,
              minLon1 = lonWest1,
              maxLon1 = lonEast1,
              minLat2 = latSouth2,
              maxLat2 = latNorth2,
              minLon2 = lonWest2,
              maxLon2 = lonEast2,
              query = query
            )
          }
          else -> throw IllegalArgumentException("Invalid map bounds.")
        }
      }
      .map { it.map { (category, count) -> VenueCategoryCount(category, count) } }

  override fun getVenueDetails(id: Long): Maybe<VenueDetails> =
    venueDetailsDao
      .selectById(id)
      .switchIfEmpty(
        coinMapApi
          .getVenue(id)
          .flatMapMaybe { (venue) ->
            venue?.takeIf(VenueDetailsResponseItem::isValid)?.let { Maybe.just(it.asEntity()) }
              ?: Maybe.empty()
          }
          .flatMap { venueDetailsDao.upsert(it).andThen(Maybe.just(it)) }
      )
      .map(VenueDetailsEntity::asDomainModel)

  override fun deleteVenueDetailsOlderThan(timestamp: Long): Completable =
    venueDetailsDao.deleteInsertedAtBeforeTimestamp(timestamp)

  override fun getVenueMarkersInLatLngBounds(
    gridMapBounds: GridMapBounds,
    query: String,
    categories: List<String>
  ): Flowable<Result<List<MapMarker>>> {
    val (bounds, latDivisor, lonDivisor) = gridMapBounds
    val (latSouth, latNorth, lonWest, lonEast) = bounds
    return waitUntilAnyVenuesExitsOrSyncCompleted()
      .andThen(
        boundsDao
          .selectExistsBounds(
            minLat = latSouth,
            maxLat = latNorth,
            minLon = lonWest,
            maxLon = lonEast
          )
          .flatMap { allExist ->
            if (allExist) {
                venueDao
                  .countMatchingQueryInBoundsSingle(
                    minLat = latSouth,
                    maxLat = latNorth,
                    minLon = lonWest,
                    maxLon = lonEast,
                    query = query,
                    categories = categories,
                    categoriesCount = categories.size
                  )
                  .map { Result.success(it) }
              } else {
                getAndInsertVenuesFromNetwork(
                    minLat = latSouth,
                    maxLat = latNorth,
                    minLon = lonWest,
                    maxLon = lonEast,
                    query = query,
                    categories = categories
                  )
                  .map { Result.success(it) }
                  .onErrorReturn { Result.failure(it) }
              }
              .toFlowable()
          }
          .flatMap { countResult ->
            countResult.fold(
              onSuccess = { count ->
                if (count < BOUNDS_MARKERS_LIMIT) {
                  venueDao
                    .selectMatchingQueryInBoundsFlowable(
                      minLat = latSouth,
                      maxLat = latNorth,
                      minLon = lonWest,
                      maxLon = lonEast,
                      query = query,
                      categories = categories,
                      categoriesCount = categories.size,
                    )
                    .map {
                      Result.success(
                        it.map { venue -> MapMarker.SingleVenue(venue.asDomainModel()) }
                      )
                    }
                } else {
                  selectCellMarkersFlowable(
                      gridCells =
                        divideBoundsIntoGrid(
                          latDivisor = latDivisor,
                          lonDivisor = lonDivisor,
                          minLat = latSouth,
                          latInc = (latNorth - latSouth) / latDivisor,
                          minLon = lonWest,
                          lonInc = (lonEast - lonWest) / lonDivisor
                        ),
                      gridCellLimit = BOUNDS_MARKERS_LIMIT / (latDivisor * lonDivisor),
                      query = query,
                      categories = categories
                    )
                    .map { Result.success(it) }
                }
              },
              onFailure = { Flowable.just(Result.failure(it)) }
            )
          }
      )
  }

  override fun anyVenuesExist(): Flowable<Boolean> = venueDao.selectCountFlowable().map { it > 0 }

  private fun waitUntilAnyVenuesExitsOrSyncCompleted(): Completable =
    venueDao.selectCountSingle().flatMapCompletable {
      if (it > 0) {
        Completable.complete()
      } else {
        syncDataSource
          .isRunningFlowable()
          .filter { isRunning -> !isRunning }
          .firstOrError()
          .ignoreElement()
      }
    }

  private fun selectCellMarkersFlowable(
    gridCells: List<MapBounds>,
    gridCellLimit: Int,
    query: String,
    categories: List<String>
  ): Flowable<List<MapMarker>> =
    venueDao
      .countMatchingQueryInMultipleBounds(
        countMatchingInMultipleBoundsQuery(gridCells, query, categories)
      )
      .switchMap {
        val (selectVenueResults, clusterResults) = it.partition { (count) -> count < gridCellLimit }
        val venuesClusters =
          clusterResults.map { (count, minLat, maxLat, minLon, maxLon) ->
            MapMarker.VenuesCluster(
              latSouth = minLat,
              latNorth = maxLat,
              lonWest = minLon,
              lonEast = maxLon,
              size = count
            )
          }
        venueDao
          .selectMatchingQueryInMultipleBounds(
            selectMatchingInMultipleBoundsQuery(
              bounds =
                selectVenueResults.map { (_, minLat, maxLat, minLon, maxLon) ->
                  MapBounds(minLat, maxLat, minLon, maxLon)
                },
              query = query,
              categories = categories
            )
          )
          .map { venues ->
            venues.map { venue -> MapMarker.SingleVenue(venue.asDomainModel()) } + venuesClusters
          }
      }

  private fun countMatchingInMultipleBoundsQuery(
    bounds: List<MapBounds>,
    query: String,
    categories: List<String>
  ): SupportSQLiteQuery =
    SimpleSQLiteQuery(
      buildString {
        bounds.forEachIndexed { index, (cellMinLat, cellMaxLat, cellMinLon, cellMaxLon) ->
          append(
            """SELECT COUNT(*) AS count, 
          | $cellMinLat AS minLat, $cellMaxLat AS maxLat, $cellMinLon AS minLon, $cellMaxLon AS maxLon 
          | FROM venue
          | WHERE lat >= $cellMinLat AND lat <= $cellMaxLat AND lon >= $cellMinLon AND lon <= $cellMaxLon 
          | AND ('$query' = '' OR LOWER(name) LIKE '%' || LOWER('$query') || '%')"""
              .trimMargin()
          )
          if (categories.isNotEmpty()) {
            append(" AND category IN (${categories.joinToString(",") { "'$it'" }})")
          }
          if (index != bounds.lastIndex) {
            append(" UNION ")
          }
        }
      }
    )

  private fun selectMatchingInMultipleBoundsQuery(
    bounds: List<MapBounds>,
    query: String,
    categories: List<String>
  ): SupportSQLiteQuery =
    SimpleSQLiteQuery(
      buildString {
        append("SELECT * FROM (")
        bounds.forEachIndexed { index, (cellMinLat, cellMaxLat, cellMinLon, cellMaxLon) ->
          append(
            """SELECT * FROM venue 
        | WHERE lat >= $cellMinLat AND lat <= $cellMaxLat AND lon >= $cellMinLon AND lon <= $cellMaxLon
        | AND ('$query' = '' OR LOWER(name) LIKE '%' || LOWER('$query') || '%')"""
              .trimMargin()
          )
          if (categories.isNotEmpty()) {
            append(
              " AND (${categories.size} = 0 OR category IN (${categories.joinToString(",") { "'$it'" }}))"
            )
          }
          append(
            """ AND EXISTS (
        | SELECT * FROM bounds WHERE whole = TRUE
        | UNION
        | SELECT * FROM bounds WHERE min_lat <= $cellMinLat AND max_lat >= $cellMaxLat AND min_lon <= $cellMinLon AND max_lon >= $cellMaxLon)"""
              .trimMargin()
          )
          if (index != bounds.lastIndex) {
            append(" UNION ")
          }
        }
        append(")")
      }
    )

  private fun divideBoundsIntoGrid(
    latDivisor: Int,
    lonDivisor: Int,
    minLat: Double,
    latInc: Double,
    minLon: Double,
    lonInc: Double
  ): List<MapBounds> {
    val gridCellBounds = ArrayList<MapBounds>(latDivisor * lonDivisor)
    repeat(latDivisor) { latMultiplier ->
      repeat(lonDivisor) { lonMultiplier ->
        val cellMinLat = minLat + latInc * latMultiplier
        val cellMaxLat = cellMinLat + latInc
        val cellMinLon = minLon + lonInc * lonMultiplier
        val cellMaxLon = cellMinLon + lonInc
        gridCellBounds.add(
          MapBounds(
            latSouth = cellMinLat,
            latNorth = cellMaxLat,
            lonWest = cellMinLon,
            lonEast = cellMaxLon
          )
        )
      }
    }
    return gridCellBounds
  }

  private fun getAndInsertVenuesFromNetwork(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String,
    categories: List<String>,
  ): Single<Int> =
    coinMapApi
      .getVenues(minLat = minLat, maxLat = maxLat, minLon = minLon, maxLon = maxLon)
      .map { it.venues?.map(VenueResponseItem::asDomainModel) ?: emptyList() }
      .flatMap { venues ->
        insertVenuesInBounds(
          venues = venues.map(Venue::asEntity),
          minLat = minLat,
          maxLat = maxLat,
          minLon = minLon,
          maxLon = maxLon,
          query = query,
          categories = categories,
        )
      }

  private fun insertVenuesInBounds(
    venues: List<VenueEntity>,
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    query: String,
    categories: List<String>
  ): Single<Int> =
    Single.fromCallable {
      db.runInTransaction(
        Callable {
          venueDao.upsert(venues)
          boundsDao.upsert(
            BoundsEntity(
              minLat = minLat,
              maxLat = maxLat,
              minLon = minLon,
              maxLon = maxLon,
              whole = false
            )
          )
          venueDao.countMatchingQueryInBounds(
            minLat = minLat,
            maxLat = maxLat,
            minLon = minLon,
            maxLon = maxLon,
            query = query,
            categories = categories,
            categoriesCount = categories.size
          )
        }
      )
    }

  private fun insertVenuesInWholeBounds(venues: List<VenueEntity>): Completable =
    Completable.fromAction {
      db.runInTransaction {
        venueDao.upsert(venues)
        boundsDao.deleteNonWhole()
        boundsDao.upsert(
          BoundsEntity(
            minLat = MapBoundsLimit.MIN_LAT,
            maxLat = MapBoundsLimit.MAX_LAT,
            minLon = MapBoundsLimit.MIN_LON,
            maxLon = MapBoundsLimit.MAX_LON,
            whole = true
          )
        )
      }
    }

  companion object {
    private const val BOUNDS_MARKERS_LIMIT = 1_000
  }
}
