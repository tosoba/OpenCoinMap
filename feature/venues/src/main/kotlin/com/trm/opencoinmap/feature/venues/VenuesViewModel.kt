package com.trm.opencoinmap.feature.venues

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.map
import androidx.paging.rxjava3.cachedIn
import com.trm.opencoinmap.core.common.util.LiveEvent
import com.trm.opencoinmap.core.domain.model.LatLng
import com.trm.opencoinmap.core.domain.model.MarkersLoadingStatus
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.usecase.GetVenuesPagingInBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.IsVenuesSyncRunningAndNoVenuesExistUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveCategoriesUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMarkersLoadingStatusUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveSheetSlideOffsetUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveUserLocationUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveVenueQueryUseCase
import com.trm.opencoinmap.core.domain.usecase.SendVenueClickedEventUseCase
import com.trm.opencoinmap.core.domain.util.RxSchedulers
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.combineLatest
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class VenuesViewModel
@Inject
constructor(
  receiveMapBoundsUseCase: ReceiveMapBoundsUseCase,
  private val getVenuesPagingInBoundsUseCase: GetVenuesPagingInBoundsUseCase,
  receiveMarkersLoadingStatusUseCase: ReceiveMarkersLoadingStatusUseCase,
  receiveSheetSlideOffsetUseCase: ReceiveSheetSlideOffsetUseCase,
  isVenuesSyncRunningAndNoVenuesExistUseCase: IsVenuesSyncRunningAndNoVenuesExistUseCase,
  private val sendVenueClickedEventUseCase: SendVenueClickedEventUseCase,
  receiveVenueQueryUseCase: ReceiveVenueQueryUseCase,
  receiveCategoriesUseCase: ReceiveCategoriesUseCase,
  receiveUserLocationUseCase: ReceiveUserLocationUseCase,
  schedulers: RxSchedulers,
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _pagingData = MutableLiveData(PagingData.empty<VenueListItem>())
  val pagingData: LiveData<PagingData<VenueListItem>> = _pagingData

  private val _isLoadingVisible = MutableLiveData(true)
  val isLoadingVisible: LiveData<Boolean> = _isLoadingVisible

  private val _loadingErrorOccurred = MutableLiveData(false)
  private val _isPagingEmpty = MutableLiveData(false)

  private val _venuesInfoState =
    MediatorLiveData<VenuesInfoState>(VenuesInfoState.Hidden).apply {
      addSource(_isLoadingVisible) {
        value =
          when {
            _loadingErrorOccurred.value == true -> VenuesInfoState.Error
            !it && _isPagingEmpty.value == true -> VenuesInfoState.Empty
            else -> VenuesInfoState.Hidden
          }
      }
      addSource(_loadingErrorOccurred) {
        value =
          when {
            it -> VenuesInfoState.Error
            _isLoadingVisible.value == false && _isPagingEmpty.value == true ->
              VenuesInfoState.Empty
            else -> VenuesInfoState.Hidden
          }
      }
      addSource(_isPagingEmpty) {
        value =
          when {
            _isLoadingVisible.value == false && it -> VenuesInfoState.Empty
            _loadingErrorOccurred.value == true -> VenuesInfoState.Error
            else -> VenuesInfoState.Hidden
          }
      }
    }
  val venuesInfoState: LiveData<VenuesInfoState> = _venuesInfoState

  private val _isVenuesListVisible =
    MediatorLiveData<Boolean>().apply {
      addSource(_isLoadingVisible) {
        value =
          !it &&
            _loadingErrorOccurred.value == false &&
            _venuesInfoState.value == VenuesInfoState.Hidden
      }
      addSource(_loadingErrorOccurred) {
        value =
          !it &&
            _isLoadingVisible.value == false &&
            _venuesInfoState.value == VenuesInfoState.Hidden
      }
      addSource(_isPagingEmpty) {
        value = !it && _isLoadingVisible.value == false && _loadingErrorOccurred.value == false
      }
    }
  val isVenuesListVisible: LiveData<Boolean> = _isVenuesListVisible

  private val _sheetSlideOffset = LiveEvent<Float>()
  val sheetSlideOffset: LiveData<Float> = _sheetSlideOffset

  init {
    isVenuesSyncRunningAndNoVenuesExistUseCase()
      .distinctUntilChanged()
      .switchMap { isRunning ->
        if (isRunning) {
          Flowable.just(PagingData.empty<VenueListItem>() to MarkersLoadingStatus.InProgress)
        } else {
          receiveMapBoundsUseCase()
            .toFlowable(BackpressureStrategy.LATEST)
            .combineLatest(
              receiveVenueQueryUseCase()
                .startWithItem("")
                .distinctUntilChanged()
                .toFlowable(BackpressureStrategy.LATEST),
              receiveCategoriesUseCase()
                .startWithItem(emptyList())
                .distinctUntilChanged()
                .toFlowable(BackpressureStrategy.LATEST),
            )
            .switchMap { (bounds, query, categories) ->
              Flowable.combineLatest(
                getVenuesPagingInBoundsUseCase(bounds, query, categories).cachedIn(viewModelScope),
                receiveUserLocationUseCase()
                  .map<UserLocation>(UserLocation::Found)
                  .startWithItem(UserLocation.Empty)
                  .toFlowable(BackpressureStrategy.LATEST),
              ) { paging, userLocation ->
                paging.map { venue ->
                  VenueListItem(
                    venue = venue,
                    distanceMeters =
                      when (userLocation) {
                        UserLocation.Empty -> {
                          null
                        }
                        is UserLocation.Found -> {
                          val results = FloatArray(1)
                          Location.distanceBetween(
                            venue.lat,
                            venue.lon,
                            userLocation.location.latitude,
                            userLocation.location.longitude,
                            results,
                          )
                          results.firstOrNull()
                        }
                      }?.toDouble(),
                  )
                }
              }
            }
            .combineLatest(
              receiveMarkersLoadingStatusUseCase().toFlowable(BackpressureStrategy.LATEST)
            )
        }
      }
      .debounce(500L, TimeUnit.MILLISECONDS)
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy { (paging, status) ->
        _isLoadingVisible.value = status is MarkersLoadingStatus.InProgress

        _loadingErrorOccurred.value = status is MarkersLoadingStatus.Error
        if (status is MarkersLoadingStatus.Error) Timber.e(status.throwable)

        _pagingData.value = paging
      }
      .addTo(compositeDisposable)

    receiveSheetSlideOffsetUseCase()
      .filter { offset -> offset >= 0f }
      .subscribeBy(onNext = _sheetSlideOffset::setValue)
      .addTo(compositeDisposable)
  }

  fun onVenueClicked(venue: Venue) {
    sendVenueClickedEventUseCase(venue)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

  fun onLoadStatesChange(loadStates: CombinedLoadStates, itemCount: Int) {
    _isPagingEmpty.value = loadStates.refresh is LoadState.NotLoading && itemCount == 0
  }

  sealed interface UserLocation {
    object Empty : UserLocation

    data class Found(val location: LatLng) : UserLocation
  }
}
