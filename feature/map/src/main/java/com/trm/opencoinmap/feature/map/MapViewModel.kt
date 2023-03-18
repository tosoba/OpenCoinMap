package com.trm.opencoinmap.feature.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.common.R as commonR
import com.trm.opencoinmap.core.common.view.get
import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.core.domain.usecase.GetMarkersInBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMessageUseCase
import com.trm.opencoinmap.core.domain.usecase.ValidateGridMapBoundsUseCase
import com.trm.opencoinmap.feature.map.model.BoundingBoxArgs
import com.trm.opencoinmap.feature.map.model.MapPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
internal class MapViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val getMarkersInBoundsUseCase: GetMarkersInBoundsUseCase,
  private val validateGridMapBoundsUseCase: ValidateGridMapBoundsUseCase,
  private val sendMessageUseCase: SendMessageUseCase,
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val boundsRelay = PublishRelay.create<GridMapBounds>()
  private val filteredBounds: Observable<GridMapBounds> =
    boundsRelay.filter(validateGridMapBoundsUseCase::invoke)

  private val retryRelay = PublishRelay.create<Unit>()

  var mapPosition by savedStateHandle.get(defaultValue = MapPosition())

  private val _isLoading = MutableLiveData(false)
  val isLoading: LiveData<Boolean> = _isLoading

  private val _markersInBounds = MutableLiveData(emptyList<MapMarker>())
  val markersInBounds: LiveData<List<MapMarker>> = _markersInBounds

  init {
    filteredBounds
      .mergeWith(retryRelay.withLatestFrom(filteredBounds) { _, bounds -> bounds })
      .debounce(1L, TimeUnit.SECONDS)
      .switchMap(getMarkersInBoundsUseCase::invoke)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(
        onNext = {
          _isLoading.value = it is Loading
          if (it is WithData) _markersInBounds.value = it.data
          sendMessage(it)
        },
        onError = { Timber.tag(TAG).e(it) }
      )
      .addTo(compositeDisposable)
  }

  private fun sendMessage(loadable: Loadable<List<MapMarker>>) {
    sendMessageUseCase(
      if (loadable is Failed) {
        Message.Shown(
          textResId = commonR.string.error_occurred,
          length = Message.Length.LONG,
          action = Message.Action(commonR.string.retry) { retryRelay.accept(Unit) }
        )
      } else {
        Message.Hidden
      }
    )
  }

  fun onBoundingBox(args: BoundingBoxArgs) {
    sendMessageUseCase(Message.Hidden)

    val (boundingBox, latDivisor, lonDivisor) = args
    boundsRelay.accept(
      GridMapBounds(
        minLat = boundingBox.latSouth,
        maxLat = boundingBox.latNorth,
        minLon = boundingBox.lonWest,
        maxLon = boundingBox.lonEast,
        latDivisor = latDivisor,
        lonDivisor = lonDivisor
      )
    )
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

  companion object {
    private const val TAG = "MAP_VM"
  }
}
