package com.trm.opencoinmap.feature.venues

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.rxjava3.cachedIn
import com.trm.opencoinmap.core.common.di.IoScheduler
import com.trm.opencoinmap.core.common.di.MainScheduler
import com.trm.opencoinmap.core.domain.model.MarkersLoadingStatus
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.usecase.GetVenuesPagingInBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMarkersLoadingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.combineLatest
import io.reactivex.rxjava3.kotlin.subscribeBy
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
  @IoScheduler private val ioScheduler: Scheduler,
  @MainScheduler private val mainScheduler: Scheduler
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _pagingData = MutableLiveData(PagingData.empty<Venue>())
  val pagingData: LiveData<PagingData<Venue>> = _pagingData

  private val _isLoadingForNewBounds = MutableLiveData(false)
  val isLoadingForNewBounds: LiveData<Boolean> = _isLoadingForNewBounds

  private val _loadingForNewBoundsFailed = MutableLiveData(false)
  val loadingForNewBoundsFailed: LiveData<Boolean> = _loadingForNewBoundsFailed

  private val _isVenuesListVisible =
    MediatorLiveData<Boolean>().apply {
      addSource(_isLoadingForNewBounds) { value = !it && _loadingForNewBoundsFailed.value == false }
      addSource(_loadingForNewBoundsFailed) { value = !it && _isLoadingForNewBounds.value == false }
    }
  val isVenuesListVisible: LiveData<Boolean> = _isVenuesListVisible

  init {
    receiveMapBoundsUseCase()
      .toFlowable(BackpressureStrategy.LATEST)
      .switchMap { getVenuesPagingInBoundsUseCase(mapBounds = it).cachedIn(viewModelScope) }
      .combineLatest(receiveMarkersLoadingStatusUseCase().toFlowable(BackpressureStrategy.LATEST))
      .subscribeOn(ioScheduler)
      .observeOn(mainScheduler)
      .subscribeBy { (pagingData, status) ->
        _isLoadingForNewBounds.value = status == MarkersLoadingStatus.IN_PROGRESS
        _loadingForNewBoundsFailed.value = status == MarkersLoadingStatus.ERROR
        _pagingData.value = pagingData
      }
      .addTo(compositeDisposable)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
