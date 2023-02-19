package com.trm.opencoinmap.core.data.repo

import com.trm.opencoinmap.core.data.mapper.asEntity
import com.trm.opencoinmap.core.database.dao.VenueDao
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.network.model.Venue
import com.trm.opencoinmap.core.network.retrofit.CoinMapApi
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class VenueRepoImpl
@Inject
constructor(
  private val openCoinMapApi: CoinMapApi,
  private val venueDao: VenueDao,
) : VenueRepo {
  override fun sync(): Completable =
    openCoinMapApi
      .getVenues()
      .map { it.venues?.map(Venue::asEntity) ?: emptyList() }
      .flatMapCompletable(venueDao::upsert)
}
