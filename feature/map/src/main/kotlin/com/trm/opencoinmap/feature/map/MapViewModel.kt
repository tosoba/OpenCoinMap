package com.trm.opencoinmap.feature.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.common.R as commonR
import com.trm.opencoinmap.core.common.view.getLiveData
import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.core.domain.usecase.CoalesceGridMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.GetInitialMapCenterUseCase
import com.trm.opencoinmap.core.domain.usecase.GetMarkersInBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.IsVenuesSyncRunningAndNoVenuesExistUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveCategoriesUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveUserLocationUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveVenueClickedEventUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveVenueQueryUseCase
import com.trm.opencoinmap.core.domain.usecase.SaveMapCenterUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMessageUseCase
import com.trm.opencoinmap.core.domain.usecase.SendVenueClickedEventUseCase
import com.trm.opencoinmap.core.domain.util.RxSchedulers
import com.trm.opencoinmap.feature.map.model.MapPosition
import com.trm.opencoinmap.feature.map.model.MapPositionUpdate
import com.trm.opencoinmap.feature.map.util.MapDefaults
import com.trm.opencoinmap.feature.map.util.toBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.IOException
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
  private val isVenuesSyncRunningAndNoVenuesExistUseCase:
    IsVenuesSyncRunningAndNoVenuesExistUseCase,
  private val saveMapCenterUseCase: SaveMapCenterUseCase,
  private val getMarkersInBoundsUseCase: GetMarkersInBoundsUseCase,
  private val coalesceGridMapBoundsUseCase: CoalesceGridMapBoundsUseCase,
  private val sendMessageUseCase: SendMessageUseCase,
  private val sendMapBoundsUseCase: SendMapBoundsUseCase,
  receiveVenueClickedEventUseCase: ReceiveVenueClickedEventUseCase,
  private val sendVenueClickedEventUseCase: SendVenueClickedEventUseCase,
  private val receiveVenueQueryUseCase: ReceiveVenueQueryUseCase,
  private val receiveCategoriesUseCase: ReceiveCategoriesUseCase,
  receiveUserLocationUseCase: ReceiveUserLocationUseCase,
  private val schedulers: RxSchedulers
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val boundsRelay = PublishRelay.create<CenteredGridBounds>()
  private val retryRelay = PublishRelay.create<Unit>()

  private val _mapPositionUpdate by
    savedStateHandle.getLiveData(
      initialValue = MapPositionUpdate(position = MapPosition(), shouldUpdate = true)
    )
  val mapPositionUpdate: LiveData<MapPositionUpdate> = _mapPositionUpdate

  private val _isLoading = MutableLiveData(false)
  val isLoading: LiveData<Boolean> = _isLoading

  private val _markersInBounds = MutableLiveData(emptyList<MapMarker>())
  val markersInBounds: LiveData<List<MapMarker>> = _markersInBounds

  private val _userLocation = MutableLiveData<LatLng>()
  val userLocation: LiveData<LatLng> = _userLocation

  init {
    getInitialMapCenterUseCase()
      .map { (latitude, longitude, zoom) ->
        MapPositionUpdate(
          position = MapPosition(latitude = latitude, longitude = longitude, zoom = zoom),
          shouldUpdate = true
        )
      }
      .defaultIfEmpty(MapPositionUpdate(position = MapPosition(), shouldUpdate = true))
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy(onSuccess = _mapPositionUpdate::setValue)
      .addTo(compositeDisposable)

    observeMapBounds()

    receiveVenueClickedEventUseCase()
      .map {
        MapPositionUpdate(
          position =
            MapPosition(
              latitude = it.lat,
              longitude = it.lon,
              zoom = _mapPositionUpdate.value?.position?.zoom ?: MapDefaults.VENUE_LOCATION_ZOOM
            ),
          shouldUpdate = true
        )
      }
      .subscribeBy(onNext = _mapPositionUpdate::setValue, onError = Timber.Forest::e)
      .addTo(compositeDisposable)

    receiveUserLocationUseCase()
      .subscribeBy(onNext = _userLocation::setValue)
      .addTo(compositeDisposable)
  }

  private fun observeMapBounds() {
    val coalescedBounds =
      boundsRelay
        .skip(1L)
        .concatMap {
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
      .doOnNext { Timber.tag("MAP_BOUNDS").e(it.toString()) }
      .doOnNext { (bounds) -> sendMapBoundsUseCase(bounds.map(GridMapBounds::bounds)) }
      .toFlowable(BackpressureStrategy.LATEST)
      .switchMap { (bounds, query, categories) ->
        isVenuesSyncRunningAndNoVenuesExistUseCase().switchMap {
          if (it) {
            Flowable.just(LoadingFirst)
          } else {
            getMarkersInBoundsUseCase(bounds = bounds, query = query, categories = categories)
              .subscribeOn(schedulers.io)
          }
        }
      }
      .doOnNext { if (it is Failed) Timber.e(it.throwable) }
      .observeOn(schedulers.main)
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
          textResId =
            if (loadable.throwable is IOException) commonR.string.no_internet_connection
            else commonR.string.error_occurred,
          length =
            if (loadable.throwable is IOException) Message.Length.INDEFINITE
            else Message.Length.LONG,
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

    _mapPositionUpdate.value = MapPositionUpdate(position = position, shouldUpdate = false)

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
