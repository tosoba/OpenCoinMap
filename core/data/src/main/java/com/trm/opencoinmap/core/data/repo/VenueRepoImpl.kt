package com.trm.opencoinmap.core.data.repo

import com.trm.opencoinmap.core.data.ext.isValid
import com.trm.opencoinmap.core.data.mapper.asDomainModel
import com.trm.opencoinmap.core.data.mapper.asEntity
import com.trm.opencoinmap.core.database.OpenCoinMapDatabase
import com.trm.opencoinmap.core.database.dao.BoundsDao
import com.trm.opencoinmap.core.database.dao.VenueDao
import com.trm.opencoinmap.core.database.entity.BoundsEntity
import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.domain.model.MarkerCluster
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.model.VenueMarkersInBounds
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.domain.util.BoundsConstants
import com.trm.opencoinmap.core.network.model.VenueResponseItem
import com.trm.opencoinmap.core.network.retrofit.CoinMapApi
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class VenueRepoImpl
@Inject
constructor(
  private val openCoinMapApi: CoinMapApi,
  private val db: OpenCoinMapDatabase,
  private val venueDao: VenueDao,
  private val boundsDao: BoundsDao
) : VenueRepo {
  override fun sync(): Completable =
    openCoinMapApi
      .getVenues()
      .map { response ->
        response.venues?.filter(VenueResponseItem::isValid)?.map(VenueResponseItem::asEntity)
          ?: emptyList()
      }
      .flatMapCompletable(::insertVenuesInWholeBounds)

  sealed interface GridCell {
    data class Markers(val venues: List<Venue>) : GridCell
    data class Cluster(val markerCluster: MarkerCluster) : GridCell
  }

  override fun getVenueMarkersInLatLngBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    latDivisor: Int,
    lonDivisor: Int,
  ): Single<VenueMarkersInBounds> =
    venueDao
      .allExistInBounds(minLat = minLat, maxLat = maxLat, minLon = minLon, maxLon = maxLon)
      .flatMap { allExist ->
        if (allExist) {
          venueDao.countInBounds(minLat = minLat, maxLat = maxLat, minLon = minLon, maxLon = maxLon)
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
            .selectInBounds(minLat = minLat, maxLat = maxLat, minLon = minLon, maxLon = maxLon)
            .map { VenueMarkersInBounds(it.map(VenueEntity::asDomainModel), emptyList()) }
        } else {
          val latInc = (maxLat - minLat) / latDivisor
          val lonInc = (maxLon - minLon) / lonDivisor
          val gridCellLimit = BOUNDS_MARKERS_LIMIT / (latDivisor * lonDivisor)
          data class Bounds(
            val minLat: Double,
            val maxLat: Double,
            val minLon: Double,
            val maxLon: Double,
          )

          val gridCellBounds = ArrayList<Bounds>(latDivisor * lonDivisor)
          repeat(latDivisor) { latMult ->
            repeat(lonDivisor) { lonMult ->
              val cellMinLat = minLat + latInc * latMult
              val cellMaxLat = maxLat + latInc * (latMult + 1)
              val cellMinLon = minLon + lonInc * lonMult
              val cellMaxLon = maxLon + lonInc * (lonMult + 1)
              gridCellBounds.add(Bounds(cellMinLat, cellMaxLat, cellMinLon, cellMaxLon))
            }
          }

          Observable.fromIterable(gridCellBounds)
            .flatMapSingle { (cellMinLat, cellMaxLat, cellMinLon, cellMaxLon) ->
              venueDao
                .countInBounds(
                  minLat = cellMinLat,
                  maxLat = cellMaxLat,
                  minLon = cellMinLon,
                  maxLon = cellMaxLon
                )
                .flatMap { count ->
                  if (count > gridCellLimit) {
                    Single.just(
                      GridCell.Cluster(
                        MarkerCluster(
                          lat = (cellMaxLat - cellMinLat) / 2.0,
                          lon = (cellMaxLon - cellMinLon) / 2.0,
                          count = count
                        )
                      )
                    )
                  } else {
                    venueDao
                      .selectInBounds(
                        minLat = cellMinLat,
                        maxLat = cellMaxLat,
                        minLon = cellMinLon,
                        maxLon = cellMaxLon
                      )
                      .map { GridCell.Markers(it.map(VenueEntity::asDomainModel)) }
                  }
                }
            }
            .toList()
            .map {
              VenueMarkersInBounds(
                it.filterIsInstance<GridCell.Markers>().flatMap(GridCell.Markers::venues),
                it.filterIsInstance<GridCell.Cluster>().map(GridCell.Cluster::markerCluster)
              )
            }
        }
      }

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
    private const val BOUNDS_MARKERS_LIMIT = 1_000
  }
}
