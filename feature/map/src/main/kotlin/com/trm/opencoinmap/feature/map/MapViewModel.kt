package com.trm.opencoinmap.feature.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay3.BehaviorRelay
import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.common.R as commonR
import com.trm.opencoinmap.core.common.view.getLiveData
import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.core.domain.usecase.CoalesceGridMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.GetInitialMapCenterUseCase
import com.trm.opencoinmap.core.domain.usecase.GetMarkersInBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveCategoriesUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveVenueClickedEventUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveVenueQueryUseCase
import com.trm.opencoinmap.core.domain.usecase.SaveMapCenterUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMessageUseCase
import com.trm.opencoinmap.core.domain.usecase.SendVenueClickedEventUseCase
import com.trm.opencoinmap.core.domain.util.RxSchedulers
import com.trm.opencoinmap.feature.map.model.MapPosition
import com.trm.opencoinmap.feature.map.util.MapDefaults
import com.trm.opencoinmap.feature.map.util.toBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import org.osmdroid.util.BoundingBox
import timber.log.Timber

@HiltViewModel
internal class MapViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  getInitialMapCenterUseCase: GetInitialMapCenterUseCase,
  saveMapCenterUseCase: SaveMapCenterUseCase,
  private val getMarkersInBoundsUseCase: GetMarkersInBoundsUseCase,
  private val coalesceGridMapBoundsUseCase: CoalesceGridMapBoundsUseCase,
  private val sendMessageUseCase: SendMessageUseCase,
  private val sendMapBoundsUseCase: SendMapBoundsUseCase,
  receiveVenueClickedEventUseCase: ReceiveVenueClickedEventUseCase,
  private val sendVenueClickedEventUseCase: SendVenueClickedEventUseCase,
  receiveVenueQueryUseCase: ReceiveVenueQueryUseCase,
  receiveCategoriesUseCase: ReceiveCategoriesUseCase,
  schedulers: RxSchedulers
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val boundsRelay = PublishRelay.create<CenteredGridBounds>()
  private val retryRelay = PublishRelay.create<Unit>()

  private val _mapPosition by savedStateHandle.getLiveData(initialValue = MapPosition())
  val mapPosition: LiveData<MapPosition> = _mapPosition

  private val _isLoading = MutableLiveData(false)
  val isLoading: LiveData<Boolean> = _isLoading

  private val _markersInBounds = MutableLiveData(emptyList<MapMarker>())
  val markersInBounds: LiveData<List<MapMarker>> = _markersInBounds

  init {
    getInitialMapCenterUseCase()
      .map { (latitude, longitude, zoom) ->
        MapPosition(latitude = latitude, longitude = longitude, zoom = zoom)
      }
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy(onSuccess = _mapPosition::setValue)
      .addTo(compositeDisposable)

    val coalescedBounds =
      boundsRelay
        .debounce(250L, TimeUnit.MILLISECONDS)
        .distinctUntilChanged()
        .doOnNext { Timber.tag("MAP_BOUNDS").e(it.toString()) }
        .flatMap {
          saveMapCenterUseCase(
              MapCenter(latitude = it.centerLat, longitude = it.centerLon, zoom = it.zoom)
            )
            .andThen(Observable.just(it))
        }
        .map { (bounds, _, centerLon) ->
          coalesceGridMapBoundsUseCase(gridMapBounds = bounds, centerLon = centerLon)
        }
    Observable.combineLatest(
        coalescedBounds.mergeWith(
          retryRelay.withLatestFrom(coalescedBounds) { _, bounds -> bounds }
        ),
        receiveVenueQueryUseCase().startWithItem("").distinctUntilChanged(),
        receiveCategoriesUseCase().startWithItem(emptyList()).distinctUntilChanged(),
      ) { bounds, query, categories ->
        Triple(bounds, query, categories)
      }
      .debounce(500L, TimeUnit.MILLISECONDS)
      .doOnNext { (bounds) -> sendMapBoundsUseCase(bounds.map(GridMapBounds::bounds)) }
      .switchMap { (bounds, query, categories) ->
        getMarkersInBoundsUseCase(bounds = bounds, query = query, categories = categories)
      }
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

    receiveVenueClickedEventUseCase()
      .map {
        MapPosition(
          latitude = it.lat,
          longitude = it.lon,
          zoom = _mapPosition.value?.zoom ?: MapDefaults.VENUE_LOCATION_ZOOM
        )
      }
      .subscribeBy(onNext = _mapPosition::setValue, onError = Timber.Forest::e)
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

  fun onMapUpdated(
    boundingBox: BoundingBox,
    position: MapPosition,
    latDivisor: Int,
    lonDivisor: Int
  ) {
    sendMessageUseCase(Message.Hidden)

    _mapPosition.value = position

    boundsRelay.accept(
      CenteredGridBounds(
        bounds =
          GridMapBounds(
            bounds = boundingBox.toBounds(),
            latDivisor = latDivisor,
            lonDivisor = lonDivisor
          ),
        centerLat = position.latitude,
        centerLon = position.longitude,
        zoom = position.zoom
      )
    )
  }

  fun onVenueMarkerClick(venue: Venue) {
    sendVenueClickedEventUseCase(venue)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

  private data class CenteredGridBounds(
    val bounds: GridMapBounds,
    val centerLat: Double,
    val centerLon: Double,
    val zoom: Double
  )
}
