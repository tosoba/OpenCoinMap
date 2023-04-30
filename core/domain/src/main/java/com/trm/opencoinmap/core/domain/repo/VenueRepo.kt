package com.trm.opencoinmap.core.domain.repo

import androidx.paging.PagingData
import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.core.domain.model.Venue
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface VenueRepo {
  fun sync(): Completable

  fun getVenuesPaging(mapBounds: MapBounds): Flowable<PagingData<Venue>>

  fun getVenueMarkersInLatLngBounds(gridMapBounds: GridMapBounds): Single<List<MapMarker>>
}
