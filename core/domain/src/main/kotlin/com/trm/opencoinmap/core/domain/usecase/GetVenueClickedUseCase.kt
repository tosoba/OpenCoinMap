package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import com.trm.opencoinmap.core.domain.util.RxSchedulers
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class GetVenueClickedUseCase
@Inject
constructor(
  private val receiveVenueClickedEventUseCase: ReceiveVenueClickedEventUseCase,
  private val venueRepo: VenueRepo,
  private val rxSchedulers: RxSchedulers
) {
  operator fun invoke(): Observable<Venue> =
    receiveVenueClickedEventUseCase().flatMapSingle {
      venueRepo.getVenueById(it).subscribeOn(rxSchedulers.io)
    }
}
