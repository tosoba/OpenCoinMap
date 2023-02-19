package com.trm.opencoinmap.core.domain.repo

import io.reactivex.rxjava3.core.Completable

interface VenueRepo {
  fun sync(): Completable
}
