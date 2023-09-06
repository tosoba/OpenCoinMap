package com.trm.opencoinmap.feature.venues

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.hadilq.liveevent.LiveEvent
import com.trm.opencoinmap.core.domain.model.MarkersLoadingStatus
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.usecase.GetVenuesPagingInBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.IsVenuesSyncRunningUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveCategoriesUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMarkersLoadingStatusUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveSheetSlideOffsetUseCase
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
internal class VenuesViewModel
@Inject
constructor(
  receiveMapBoundsUseCase: ReceiveMapBoundsUseCase,
  private val getVenuesPagingInBoundsUseCase: GetVenuesPagingInBoundsUseCase,
  receiveMarkersLoadingStatusUseCase: ReceiveMarkersLoadingStatusUseCase,
  receiveSheetSlideOffsetUseCase: ReceiveSheetSlideOffsetUseCase,
  isVenuesSyncRunningUseCase: IsVenuesSyncRunningUseCase,
  private val sendVenueClickedEventUseCase: SendVenueClickedEventUseCase,
  receiveVenueQueryUseCase: ReceiveVenueQueryUseCase,
  receiveCategoriesUseCase: ReceiveCategoriesUseCase,
  schedulers: RxSchedulers
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _pagingData = MutableLiveData(PagingData.empty<Venue>())
  val pagingData: LiveData<PagingData<Venue>> = _pagingData

  private val _isLoadingVisible = MutableLiveData(true)
  val isLoadingVisible: LiveData<Boolean> = _isLoadingVisible

  private val _isErrorVisible = MutableLiveData(false)
  val isErrorVisible: LiveData<Boolean> = _isErrorVisible

  private val _isPagingEmpty = MutableLiveData(false)

  private val _isEmptyViewVisible =
    MediatorLiveData<Boolean>().apply {
      addSource(_isLoadingVisible) {
        value = !it && _isErrorVisible.value == false && _isPagingEmpty.value == true
      }
      addSource(_isErrorVisible) {
        value = !it && _isLoadingVisible.value == false && _isPagingEmpty.value == true
      }
      addSource(_isPagingEmpty) {
        value = it && _isLoadingVisible.value == false && _isErrorVisible.value == false
      }
    }
  val isEmptyViewVisible: LiveData<Boolean> = _isEmptyViewVisible

  private val _isVenuesListVisible =
    MediatorLiveData<Boolean>().apply {
      addSource(_isLoadingVisible) {
        value = !it && _isErrorVisible.value == false && _isEmptyViewVisible.value == false
      }
      addSource(_isErrorVisible) {
        value = !it && _isLoadingVisible.value == false && _isEmptyViewVisible.value == false
      }
      addSource(_isPagingEmpty) {
        value = !it && _isLoadingVisible.value == false && _isPagingEmpty.value == false
      }
    }
  val isVenuesListVisible: LiveData<Boolean> = _isVenuesListVisible

  private val _sheetSlideOffset = LiveEvent<Float>()
  val sheetSlideOffset: LiveData<Float> = _sheetSlideOffset

  init {
    isVenuesSyncRunningUseCase()
      .toFlowable(BackpressureStrategy.LATEST)
      .switchMap { isRunning ->
        if (isRunning) {
          Flowable.just(PagingData.empty())
        } else {
          Flowable.combineLatest(
              receiveMapBoundsUseCase().toFlowable(BackpressureStrategy.LATEST),
              receiveVenueQueryUseCase()
                .startWithItem("")
                .distinctUntilChanged()
                .toFlowable(BackpressureStrategy.LATEST),
              receiveCategoriesUseCase()
                .startWithItem(emptyList())
                .distinctUntilChanged()
                .toFlowable(BackpressureStrategy.LATEST),
            ) { bounds, query, categories ->
              Triple(bounds, query, categories)
            }
            .switchMap { (bounds, query, categories) ->
              getVenuesPagingInBoundsUseCase(
                  mapBounds = bounds,
                  query = query,
                  categories = categories
                )
                .cachedIn(viewModelScope)
            }
        }
      }
      .combineLatest(receiveMarkersLoadingStatusUseCase().toFlowable(BackpressureStrategy.LATEST))
      .debounce(500L, TimeUnit.MILLISECONDS)
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy { (pagingData, status) ->
        _isLoadingVisible.value = status is MarkersLoadingStatus.InProgress

        _isErrorVisible.value = status is MarkersLoadingStatus.Error
        if (status is MarkersLoadingStatus.Error) Timber.e(status.throwable)

        _pagingData.value = pagingData
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
}
