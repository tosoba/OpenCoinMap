package com.trm.opencoinmap.core.data.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.rxjava3.flowable
import com.trm.opencoinmap.core.data.ext.isValid
import com.trm.opencoinmap.core.data.mapper.asDomainModel
import com.trm.opencoinmap.core.data.mapper.asEntity
import com.trm.opencoinmap.core.database.OpenCoinMapDatabase
import com.trm.opencoinmap.core.database.entity.BoundsEntity
import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.domain.sync.SyncDataSource
import com.trm.opencoinmap.core.domain.util.MapBoundsLimit
import com.trm.opencoinmap.core.network.model.VenueResponseItem
import com.trm.opencoinmap.core.network.retrofit.CoinMapApi
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
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

  override fun sync(): Completable {
    syncDataSource.isRunning = true
    return coinMapApi
      .getVenues()
      .map { response ->
        response.venues?.filter(VenueResponseItem::isValid)?.map(VenueResponseItem::asEntity)
          ?: emptyList()
      }
      .flatMapCompletable(::insertVenuesInWholeBounds)
      .doAfterTerminate { syncDataSource.isRunning = false }
  }

  override fun getVenuesPagingInBounds(mapBounds: MapBounds): Flowable<PagingData<Venue>> {
    val (minLat, maxLat, minLon, maxLon) = mapBounds
    return Pager(
        config = PagingConfig(pageSize = 50, enablePlaceholders = false, initialLoadSize = 50)
      ) {
        venueDao.selectPageInBounds(
          minLat = minLat,
          maxLat = maxLat,
          minLon = minLon,
          maxLon = maxLon
        )
      }
      .flowable
      .map { it.map(VenueEntity::asDomainModel) }
  }

  override fun getCategoriesInBounds(mapBounds: MapBounds): Flowable<List<VenueCategoryCount>> {
    val (minLat, maxLat, minLon, maxLon) = mapBounds
    return venueDao
      .selectDistinctCategoriesInBounds(
        minLat = minLat,
        maxLat = maxLat,
        minLon = minLon,
        maxLon = maxLon
      )
      .map { it.map { (category, count) -> VenueCategoryCount(category, count) } }
  }

  override fun getVenueMarkersInLatLngBounds(
    gridMapBounds: GridMapBounds
  ): Single<List<MapMarker>> {
    val (bounds, latDivisor, lonDivisor) = gridMapBounds
    val (minLat, maxLat, minLon, maxLon) = bounds
    return waitUntilSyncCompleted()
      .andThen(
        venueDao
          .allExistInBounds(minLat = minLat, maxLat = maxLat, minLon = minLon, maxLon = maxLon)
          .flatMap { allExist ->
            if (allExist) {
              venueDao.countInBoundsSingle(
                minLat = minLat,
                maxLat = maxLat,
                minLon = minLon,
                maxLon = maxLon
              )
            } else {
              getAndInsertVenuesFromNetwork(
                minLat = minLat,
                maxLat = maxLat,
                minLon = minLon,
                maxLon = maxLon
              )
            }
          }
          .flatMap { count ->
            if (count < BOUNDS_MARKERS_LIMIT) {
              venueDao
                .selectInBoundsSingle(
                  minLat = minLat,
                  maxLat = maxLat,
                  minLon = minLon,
                  maxLon = maxLon
                )
                .map { it.map { venue -> MapMarker.SingleVenue(venue.asDomainModel()) } }
            } else {
              val latInc = (maxLat - minLat) / latDivisor
              val lonInc = (maxLon - minLon) / lonDivisor
              val gridCellLimit = BOUNDS_MARKERS_LIMIT / (latDivisor * lonDivisor)
              val gridCells =
                divideBoundsIntoGrid(
                  latDivisor = latDivisor,
                  lonDivisor = lonDivisor,
                  minLat = minLat,
                  latInc = latInc,
                  minLon = minLon,
                  lonInc = lonInc
                )

              Single.fromCallable {
                  selectCellMarkers(gridCells = gridCells, gridCellLimit = gridCellLimit)
                }
                .map { cells ->
                  cells.flatMap { cell ->
                    when (cell) {
                      is GridCellMarkers.Cluster -> {
                        listOf(
                          MapMarker.VenuesCluster(
                            minLat = cell.minLat,
                            maxLat = cell.maxLat,
                            minLon = cell.minLon,
                            maxLon = cell.maxLon,
                            size = cell.count
                          )
                        )
                      }
                      is GridCellMarkers.Venues -> {
                        cell.venues.map(MapMarker::SingleVenue)
                      }
                    }
                  }
                }
            }
          }
      )
  }

  private fun waitUntilSyncCompleted(): Completable =
    syncDataSource
      .isRunningObservable()
      .filter { isRunning -> !isRunning }
      .firstOrError()
      .ignoreElement()

  private fun selectCellMarkers(
    gridCells: List<Bounds>,
    gridCellLimit: Int
  ): List<GridCellMarkers> =
    db.runInTransaction(
      Callable {
        gridCells.map { (cellMinLat, cellMaxLat, cellMinLon, cellMaxLon) ->
          val countInCell =
            venueDao.countInBounds(
              minLat = cellMinLat,
              maxLat = cellMaxLat,
              minLon = cellMinLon,
              maxLon = cellMaxLon
            )
          if (countInCell > gridCellLimit) {
            GridCellMarkers.Cluster(
              minLat = cellMinLat,
              maxLat = cellMaxLat,
              minLon = cellMinLon,
              maxLon = cellMaxLon,
              count = countInCell
            )
          } else {
            GridCellMarkers.Venues(
              venueDao
                .selectInBounds(
                  minLat = cellMinLat,
                  maxLat = cellMaxLat,
                  minLon = cellMinLon,
                  maxLon = cellMaxLon
                )
                .map(VenueEntity::asDomainModel)
            )
          }
        }
      }
    )

  private sealed interface GridCellMarkers {
    data class Venues(val venues: List<Venue>) : GridCellMarkers
    data class Cluster(
      val minLat: Double,
      val maxLat: Double,
      val minLon: Double,
      val maxLon: Double,
      val count: Int
    ) : GridCellMarkers
  }

  private fun divideBoundsIntoGrid(
    latDivisor: Int,
    lonDivisor: Int,
    minLat: Double,
    latInc: Double,
    minLon: Double,
    lonInc: Double
  ): List<Bounds> {
    val gridCellBounds = ArrayList<Bounds>(latDivisor * lonDivisor)
    repeat(latDivisor) { latMultiplier ->
      repeat(lonDivisor) { lonMultiplier ->
        val cellMinLat = minLat + latInc * latMultiplier
        val cellMaxLat = cellMinLat + latInc
        val cellMinLon = minLon + lonInc * lonMultiplier
        val cellMaxLon = cellMinLon + lonInc
        gridCellBounds.add(
          Bounds(minLat = cellMinLat, maxLat = cellMaxLat, minLon = cellMinLon, maxLon = cellMaxLon)
        )
      }
    }
    return gridCellBounds
  }

  private data class Bounds(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double,
  )

  private fun getAndInsertVenuesFromNetwork(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
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
            maxLon = maxLon
          )
          .toSingleDefault(venues.size)
      }

  private fun insertVenuesInBounds(
    venues: List<VenueEntity>,
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Completable =
    Completable.fromAction {
      db.runInTransaction {
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
      }
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
    private const val BOUNDS_MARKERS_LIMIT = 10_000
  }
}
