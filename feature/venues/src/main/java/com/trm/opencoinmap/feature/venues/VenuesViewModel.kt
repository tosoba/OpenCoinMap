package com.trm.opencoinmap.feature.venues

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.hadilq.liveevent.LiveEvent
import com.trm.opencoinmap.core.common.di.RxSchedulers
import com.trm.opencoinmap.core.domain.model.MarkersLoadingStatus
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.usecase.GetVenuesPagingInBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.IsVenuesSyncRunningUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMarkersLoadingStatusUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveSheetSlideOffsetUseCase
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
  schedulers: RxSchedulers
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _pagingData = MutableLiveData(PagingData.empty<Venue>())
  val pagingData: LiveData<PagingData<Venue>> = _pagingData

  private val _isLoadingProgressLayoutVisible = MutableLiveData(true)
  val isLoadingProgressLayoutVisible: LiveData<Boolean> = _isLoadingProgressLayoutVisible

  private val _loadingForNewBoundsFailed = MutableLiveData(false)
  val loadingForNewBoundsFailed: LiveData<Boolean> = _loadingForNewBoundsFailed

  private val _isVenuesListVisible =
    MediatorLiveData<Boolean>().apply {
      addSource(_isLoadingProgressLayoutVisible) {
        value = !it && _loadingForNewBoundsFailed.value == false
      }
      addSource(_loadingForNewBoundsFailed) {
        value = !it && _isLoadingProgressLayoutVisible.value == false
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
          Flowable.just(PagingData.empty<Venue>() to MarkersLoadingStatus.IN_PROGRESS)
        } else {
          receiveMapBoundsUseCase()
            .toFlowable(BackpressureStrategy.LATEST)
            .switchMap { bounds -> getVenuesPagingInBoundsUseCase(bounds).cachedIn(viewModelScope) }
            .combineLatest(
              receiveMarkersLoadingStatusUseCase().toFlowable(BackpressureStrategy.LATEST)
            )
            .debounce(250L, TimeUnit.MILLISECONDS)
        }
      }
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy { (pagingData, status) ->
        _isLoadingProgressLayoutVisible.value = status == MarkersLoadingStatus.IN_PROGRESS
        _loadingForNewBoundsFailed.value = status == MarkersLoadingStatus.ERROR
        _pagingData.value = pagingData
      }
      .addTo(compositeDisposable)

    receiveSheetSlideOffsetUseCase()
      .filter { offset -> offset >= 0f }
      .subscribeBy(onNext = _sheetSlideOffset::setValue)
      .addTo(compositeDisposable)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
