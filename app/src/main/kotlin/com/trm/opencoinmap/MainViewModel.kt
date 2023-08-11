package com.trm.opencoinmap

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hadilq.liveevent.LiveEvent
import com.trm.opencoinmap.core.common.view.getLiveData
import com.trm.opencoinmap.core.domain.model.Message
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.usecase.ReceiveCategoriesListLayoutEventUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMessageUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveVenueClickedEventUseCase
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
  savedStateHandle: SavedStateHandle,
  receiveMessageUseCase: ReceiveMessageUseCase,
  private val sendSheetSlideOffsetUseCase: SendSheetSlideOffsetUseCase,
  receiveCategoriesListLayoutEventUseCase: ReceiveCategoriesListLayoutEventUseCase,
  receiveVenueClickedEventUseCase: ReceiveVenueClickedEventUseCase,
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _searchQuery by savedStateHandle.getLiveData(initialValue = "")
  val searchQuery: LiveData<String> = _searchQuery
  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  private val _searchFocused by savedStateHandle.getLiveData(initialValue = false)
  val searchFocused: LiveData<Boolean> = _searchFocused
  fun setSearchFocused(focused: Boolean) {
    _searchFocused.value = focused
  }

  private val _snackbarMessage = LiveEvent<Message>()
  val snackbarMessage: LiveData<Message> = _snackbarMessage

  private val _categoriesUpdatedEvent = LiveEvent<Unit>()
  val categoriesUpdatedEvent: LiveData<Unit> = _categoriesUpdatedEvent

  private val _venueClicked = LiveEvent<Venue>()
  val venueClicked: LiveData<Venue> = _venueClicked

  init {
    receiveMessageUseCase()
      .subscribeBy(onNext = _snackbarMessage::setValue)
      .addTo(compositeDisposable)

    receiveCategoriesListLayoutEventUseCase()
      .subscribeBy(onNext = _categoriesUpdatedEvent::setValue)
      .addTo(compositeDisposable)

    receiveVenueClickedEventUseCase()
      .subscribeBy(onNext = _venueClicked::setValue)
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
