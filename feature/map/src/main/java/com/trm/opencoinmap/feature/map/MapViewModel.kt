package com.trm.opencoinmap.feature.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.common.R as commonR
import com.trm.opencoinmap.core.common.di.RxSchedulers
import com.trm.opencoinmap.core.common.view.get
import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.core.domain.usecase.CoalesceGridMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.GetMarkersInBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMessageUseCase
import com.trm.opencoinmap.feature.map.model.MapPosition
import com.trm.opencoinmap.feature.map.util.toBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.BoundingBox
import timber.log.Timber

@HiltViewModel
internal class MapViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val getMarkersInBoundsUseCase: GetMarkersInBoundsUseCase,
  private val coalesceGridMapBoundsUseCase: CoalesceGridMapBoundsUseCase,
  private val sendMessageUseCase: SendMessageUseCase,
  private val sendMapBoundsUseCase: SendMapBoundsUseCase,
  schedulers: RxSchedulers
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val boundsRelay = PublishRelay.create<CenteredGridBounds>()
  private val retryRelay = PublishRelay.create<Unit>()

  var mapPosition by savedStateHandle.get(defaultValue = MapPosition())

  private val _isLoading = MutableLiveData(false)
  val isLoading: LiveData<Boolean> = _isLoading

  private val _markersInBounds = MutableLiveData(emptyList<MapMarker>())
  val markersInBounds: LiveData<List<MapMarker>> = _markersInBounds

  init {
    val coalescedBounds =
      boundsRelay.map { (bounds, _, centerLon) ->
        coalesceGridMapBoundsUseCase(gridMapBounds = bounds, centerLon = centerLon)
      }
    coalescedBounds
      .mergeWith(retryRelay.withLatestFrom(coalescedBounds) { _, bounds -> bounds })
      .debounce(1L, TimeUnit.SECONDS)
      .doOnNext { sendMapBoundsUseCase(it.map(GridMapBounds::bounds)) }
      .switchMap(getMarkersInBoundsUseCase::invoke)
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .doOnNext { if (it is Failed) Timber.e(it.throwable) }
      .subscribeBy(
        onNext = {
          _isLoading.value = it is Loading
          if (it is WithData) _markersInBounds.value = it.data
          sendMessage(it)
        },
        onError = Timber.Forest::e
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

  fun onBoundingBoxChanged(
    boundingBox: BoundingBox,
    center: IGeoPoint,
    latDivisor: Int,
    lonDivisor: Int
  ) {
    sendMessageUseCase(Message.Hidden)

    val bounds = boundingBox.toBounds()
    boundsRelay.accept(
      CenteredGridBounds(
        bounds = GridMapBounds(bounds = bounds, latDivisor = latDivisor, lonDivisor = lonDivisor),
        centerLat = center.latitude,
        centerLon = center.longitude
      )
    )
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

  private data class CenteredGridBounds(
    val bounds: GridMapBounds,
    val centerLat: Double,
    val centerLon: Double
  )
}
