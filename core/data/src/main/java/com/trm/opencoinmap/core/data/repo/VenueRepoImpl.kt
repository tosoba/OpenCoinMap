package com.trm.opencoinmap.core.data.repo

import com.trm.opencoinmap.core.data.ext.isValid
import com.trm.opencoinmap.core.data.mapper.asDomainModel
import com.trm.opencoinmap.core.data.mapper.asEntity
import com.trm.opencoinmap.core.database.OpenCoinMapDatabase
import com.trm.opencoinmap.core.database.dao.BoundsDao
import com.trm.opencoinmap.core.database.dao.VenueDao
import com.trm.opencoinmap.core.database.entity.BoundsEntity
import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.domain.util.BoundsConstants
import com.trm.opencoinmap.core.network.model.VenueResponseItem
import com.trm.opencoinmap.core.network.retrofit.CoinMapApi
import io.reactivex.rxjava3.core.Completable
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

  override fun getVenuesInLatLngBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    latDivisor: Int,
    lonDivisor: Int,
  ): Single<List<Venue>> =
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
        if (count < 1_000) {
          venueDao.selectInBounds(minLat, maxLat, minLon, maxLon).map {
            it.map(VenueEntity::asDomainModel)
          }
        } else {
          Single.just(emptyList())
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
}
