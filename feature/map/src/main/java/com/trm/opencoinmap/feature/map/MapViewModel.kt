package com.trm.opencoinmap.feature.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.common.view.get
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.usecase.VenuesInBoundsSubjectUseCase
import com.trm.opencoinmap.feature.map.model.MapPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import org.osmdroid.util.BoundingBox

@HiltViewModel
internal class MapViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val venuesInBoundsSubjectUseCase: VenuesInBoundsSubjectUseCase,
) : ViewModel() {
  var mapPosition: MapPosition by savedStateHandle.get(defaultValue = MapPosition())

  val venuesInBounds: Observable<List<Venue>> =
    venuesInBoundsSubjectUseCase
      .observable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())

  fun onBoundingBox(boundingBox: BoundingBox, latDivisor: Int, lonDivisor: Int) {
    venuesInBoundsSubjectUseCase.onNext(
      VenuesInBoundsSubjectUseCase.Args(
        minLat = boundingBox.latSouth,
        maxLat = boundingBox.latNorth,
        minLon = boundingBox.lonWest,
        maxLon = boundingBox.lonEast,
        latDivisor = latDivisor,
        lonDivisor = lonDivisor
      )
    )
  }
}
