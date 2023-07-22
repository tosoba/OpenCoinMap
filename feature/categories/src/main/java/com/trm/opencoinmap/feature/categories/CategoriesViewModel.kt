package com.trm.opencoinmap.feature.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.common.di.RxSchedulers
import com.trm.opencoinmap.core.domain.model.MarkersLoadingStatus
import com.trm.opencoinmap.core.domain.usecase.GetCategoriesInBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMarkersLoadingStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.combineLatest
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
internal class CategoriesViewModel
@Inject
constructor(
  receiveMapBoundsUseCase: ReceiveMapBoundsUseCase,
  receiveMarkersLoadingStatusUseCase: ReceiveMarkersLoadingStatusUseCase,
  private val getCategoriesInBoundsUseCase: GetCategoriesInBoundsUseCase,
  schedulers: RxSchedulers
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _categories = MutableLiveData(emptyList<String>())
  val categories: LiveData<List<String>> = _categories

  private val _isLoadingForNewBounds = MutableLiveData(false)
  val isLoadingForNewBounds: LiveData<Boolean> = _isLoadingForNewBounds

  private val _loadingForNewBoundsFailed = MutableLiveData(false)
  val loadingForNewBoundsFailed: LiveData<Boolean> = _loadingForNewBoundsFailed

  private val _isCategoriesListVisible =
    MediatorLiveData<Boolean>().apply {
      addSource(_isLoadingForNewBounds) { value = !it && _loadingForNewBoundsFailed.value == false }
      addSource(_loadingForNewBoundsFailed) { value = !it && _isLoadingForNewBounds.value == false }
    }
  val isCategoriesListVisible: LiveData<Boolean> = _isCategoriesListVisible

  init {
    receiveMapBoundsUseCase()
      .toFlowable(BackpressureStrategy.LATEST)
      .switchMap(getCategoriesInBoundsUseCase::invoke)
      .combineLatest(receiveMarkersLoadingStatusUseCase().toFlowable(BackpressureStrategy.LATEST))
      .debounce(250L, TimeUnit.MILLISECONDS)
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy { (categories, status) ->
        _isLoadingForNewBounds.value = status == MarkersLoadingStatus.IN_PROGRESS
        _loadingForNewBoundsFailed.value = status == MarkersLoadingStatus.ERROR
        _categories.value = categories
      }
      .addTo(compositeDisposable)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
