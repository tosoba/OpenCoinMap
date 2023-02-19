package com.trm.opencoinmap.core.data.repo

import com.trm.opencoinmap.core.data.ext.isValid
import com.trm.opencoinmap.core.data.mapper.asDomainModel
import com.trm.opencoinmap.core.data.mapper.asEntity
import com.trm.opencoinmap.core.database.dao.VenueDao
import com.trm.opencoinmap.core.database.entity.VenueEntity
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.network.model.VenueResponseItem
import com.trm.opencoinmap.core.network.retrofit.CoinMapApi
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class VenueRepoImpl
@Inject
constructor(private val openCoinMapApi: CoinMapApi, private val venueDao: VenueDao) : VenueRepo {
  override fun sync(): Completable =
    openCoinMapApi
      .getVenues()
      .map { response ->
        response.venues?.filter(VenueResponseItem::isValid)?.map(VenueResponseItem::asEntity)
          ?: emptyList()
      }
      .flatMapCompletable(venueDao::upsert)

  override fun getVenuesInLatLngBounds(
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
  ): Single<List<Venue>> =
    venueDao.anyExists().flatMap { anyExists ->
      if (anyExists) {
        venueDao
          .selectInBounds(minLat = minLat, maxLat = maxLat, minLon = minLon, maxLon = maxLon)
          .map { it.map(VenueEntity::asDomainModel) }
      } else {
        openCoinMapApi
          .getVenues(minLat = minLat, maxLat = maxLat, minLon = minLon, maxLon = maxLon)
          .map { it.venues?.map(VenueResponseItem::asDomainModel) ?: emptyList() }
      }
    }
}
