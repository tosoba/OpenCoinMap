package com.trm.opencoinmap.core.domain.repo

import androidx.paging.PagingData
import com.trm.opencoinmap.core.domain.model.GridMapBounds
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

interface VenueRepo {
  fun sync(): Completable

  fun getVenuesPagingInBounds(
    mapBounds: List<MapBounds>,
    query: String,
    categories: List<String>
  ): Flowable<PagingData<Venue>>

  fun getVenueMarkersInLatLngBounds(
    gridMapBounds: GridMapBounds,
    query: String,
    categories: List<String>
  ): Single<List<MapMarker>>

  fun getCategories(): Flowable<List<VenueCategoryCount>>
}
