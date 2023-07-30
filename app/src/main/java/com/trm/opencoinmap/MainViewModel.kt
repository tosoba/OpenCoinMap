package com.trm.opencoinmap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hadilq.liveevent.LiveEvent
import com.trm.opencoinmap.core.common.di.RxSchedulers
import com.trm.opencoinmap.core.domain.model.Message
import com.trm.opencoinmap.core.domain.usecase.GetVenuesCountUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveCategoriesListLayoutEventUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMessageUseCase
import com.trm.opencoinmap.core.domain.usecase.SendSheetSlideOffsetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel
@Inject
constructor(
  receiveMessageUseCase: ReceiveMessageUseCase,
  private val sendSheetSlideOffsetUseCase: SendSheetSlideOffsetUseCase,
  receiveCategoriesListLayoutEventUseCase: ReceiveCategoriesListLayoutEventUseCase,
  getVenuesCountUseCase: GetVenuesCountUseCase,
  schedulers: RxSchedulers,
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _bottomSheetVisible = MutableLiveData(false)
  val bottomSheetVisible: LiveData<Boolean> = _bottomSheetVisible

  private val _snackbarMessage = LiveEvent<Message>()
  val snackbarMessage: LiveData<Message> = _snackbarMessage

  private val _categoriesUpdatedEvent = LiveEvent<Unit>()
  val categoriesUpdatedEvent: LiveData<Unit> = _categoriesUpdatedEvent

  init {
    getVenuesCountUseCase()
      .map { it > 0 }
      .distinctUntilChanged()
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy(onNext = _bottomSheetVisible::setValue)
      .addTo(compositeDisposable)

    receiveMessageUseCase()
      .subscribeBy(onNext = _snackbarMessage::setValue)
      .addTo(compositeDisposable)

    receiveCategoriesListLayoutEventUseCase()
      .subscribeBy(onNext = _categoriesUpdatedEvent::setValue)
      .addTo(compositeDisposable)
  }

  fun onSearchViewsSizeMeasure(@BottomSheetBehavior.State sheetState: Int) {
    sendSheetSlideOffsetUseCase(if (sheetState == BottomSheetBehavior.STATE_EXPANDED) 1f else 0f)
  }

  fun onSheetSlide(slideOffset: Float) {
    sendSheetSlideOffsetUseCase(slideOffset)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
