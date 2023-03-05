package com.trm.opencoinmap.feature.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.common.view.get
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.core.domain.usecase.MarkersInBoundsSubjectUseCase
import com.trm.opencoinmap.feature.map.model.MapPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import org.osmdroid.util.BoundingBox
import timber.log.Timber

@HiltViewModel
internal class MapViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val markersInBoundsSubjectUseCase: MarkersInBoundsSubjectUseCase,
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  var mapPosition: MapPosition by savedStateHandle.get(defaultValue = MapPosition())

  private val _markersInBounds: MutableLiveData<List<MapMarker>> = MutableLiveData(emptyList())
  val markersInBounds: LiveData<List<MapMarker>> = _markersInBounds

  init {
    markersInBoundsSubjectUseCase
      .observable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(onNext = _markersInBounds::setValue, onError = { Timber.tag(TAG).e(it) })
      .addTo(compositeDisposable)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

  fun onBoundingBox(boundingBox: BoundingBox, latDivisor: Int, lonDivisor: Int) {
    markersInBoundsSubjectUseCase.onNext(
      MarkersInBoundsSubjectUseCase.Args(
        minLat = boundingBox.latSouth,
        maxLat = boundingBox.latNorth,
        minLon = boundingBox.lonWest,
        maxLon = boundingBox.lonEast,
        latDivisor = latDivisor,
        lonDivisor = lonDivisor
      )
    )
  }

  companion object {
    private const val TAG = "MAP_VM"
  }
}
