package com.trm.opencoinmap.core.data.repo

import com.trm.opencoinmap.core.data.ext.isValid
import com.trm.opencoinmap.core.data.mapper.asDomainModel
import com.trm.opencoinmap.core.data.mapper.asEntity
import com.trm.opencoinmap.core.database.OpenCoinMapDatabase
import com.trm.opencoinmap.core.database.dao.BoundsDao
import com.trm.opencoinmap.core.database.dao.VenueDao
import com.trm.opencoinmap.core.database.entity.BoundsEntity
import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.domain.util.BoundsConstants
import com.trm.opencoinmap.core.network.model.VenueResponseItem
import com.trm.opencoinmap.core.network.retrofit.CoinMapApi
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.Callable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VenueRepoImpl
@Inject
constructor(
  private val openCoinMapApi: CoinMapApi,
  private val db: OpenCoinMapDatabase,
  private val venueDao: VenueDao,
  private val boundsDao: BoundsDao
) : VenueRepo {
  private val syncRunning = BehaviorSubject.createDefault(false)

  override fun sync(): Completable {
    syncRunning.onNext(true)
    return openCoinMapApi
      .getVenues()
      .map { response ->
        response.venues?.filter(VenueResponseItem::isValid)?.map(VenueResponseItem::asEntity)
          ?: emptyList()
      }
      .flatMapCompletable(::insertVenuesInWholeBounds)
      .doAfterTerminate { syncRunning.onNext(false) }
  }

  private fun waitUntilSyncCompleted(): Completable =
    syncRunning.filter { isRunning -> !isRunning }.firstOrError().ignoreElement()

  override fun getVenueMarkersInLatLngBounds(bounds: GridMapBounds): Single<List<MapMarker>> {
    val (minLat, maxLat, minLon, maxLon, latDivisor, lonDivisor) = bounds
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
    openCoinMapApi
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
            minLat = BoundsConstants.MIN_LAT,
            maxLat = BoundsConstants.MAX_LAT,
            minLon = BoundsConstants.MIN_LON,
            maxLon = BoundsConstants.MAX_LON,
            whole = true
          )
        )
      }
    }

  companion object {
    private const val BOUNDS_MARKERS_LIMIT = 10_000
  }
}
