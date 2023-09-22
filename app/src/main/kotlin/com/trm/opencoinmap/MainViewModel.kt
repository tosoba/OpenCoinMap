package com.trm.opencoinmap

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.hadilq.liveevent.LiveEvent
import com.trm.opencoinmap.core.common.view.getLiveData
import com.trm.opencoinmap.core.domain.model.Message
import com.trm.opencoinmap.core.domain.model.Venue
import com.trm.opencoinmap.core.domain.usecase.GetCurrentUserLocationUseCase
import com.trm.opencoinmap.core.domain.usecase.GetTrimmedQueryOrEmptyUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveCategoriesListLayoutEventUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveMessageUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveVenueClickedEventUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveWebViewScrollableUpwardsUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMessageUseCase
import com.trm.opencoinmap.core.domain.usecase.SendSheetSlideOffsetUseCase
import com.trm.opencoinmap.core.domain.usecase.SendUserLocationUseCase
import com.trm.opencoinmap.core.domain.usecase.SendVenueQueryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
internal class MainViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  receiveMessageUseCase: ReceiveMessageUseCase,
  private val sendMessageUseCase: SendMessageUseCase,
  private val sendSheetSlideOffsetUseCase: SendSheetSlideOffsetUseCase,
  receiveCategoriesListLayoutEventUseCase: ReceiveCategoriesListLayoutEventUseCase,
  receiveVenueClickedEventUseCase: ReceiveVenueClickedEventUseCase,
  sendVenueQueryUseCase: SendVenueQueryUseCase,
  getTrimmedQueryOrEmptyUseCase: GetTrimmedQueryOrEmptyUseCase,
  receiveWebViewScrollableUpwardsUseCase: ReceiveWebViewScrollableUpwardsUseCase,
  private val getCurrentUserLocationUseCase: GetCurrentUserLocationUseCase,
  private val sendUserLocationUseCase: SendUserLocationUseCase
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _bottomSheetDestinationId = MutableLiveData(R.id.venues_fragment)
  private val _bottomSheetVenueName = MutableLiveData<String?>()

  val searchQuery by savedStateHandle.getLiveData(initialValue = "")
  val searchFocused by savedStateHandle.getLiveData(initialValue = false)

  private val _searchBarLeadingIconMode =
    MediatorLiveData<MainSearchBarLeadingIconMode>().apply {
      addSource(searchFocused) {
        value =
          if (it || _bottomSheetDestinationId.value != R.id.venues_fragment) {
            MainSearchBarLeadingIconMode.BACK
          } else {
            MainSearchBarLeadingIconMode.SEARCH
          }
      }
      addSource(_bottomSheetDestinationId) {
        value =
          if (it != R.id.venues_fragment || searchFocused.value == true) {
            MainSearchBarLeadingIconMode.BACK
          } else {
            MainSearchBarLeadingIconMode.SEARCH
          }
      }
    }
  val searchBarLeadingIconMode: LiveData<MainSearchBarLeadingIconMode> = _searchBarLeadingIconMode

  private val _searchBarTrailingIconVisible =
    MediatorLiveData<Boolean>().apply {
      addSource(_bottomSheetDestinationId) {
        value = it == R.id.venues_fragment && !searchQuery.value.isNullOrBlank()
      }
      addSource(searchQuery) {
        value = _bottomSheetDestinationId.value == R.id.venues_fragment && !it.isNullOrBlank()
      }
    }
  val searchBarTrailingIconVisible: LiveData<Boolean> = _searchBarTrailingIconVisible

  private val _searchBarQuery =
    MediatorLiveData<String>().apply {
      addSource(_bottomSheetDestinationId) {
        value =
          if (it == R.id.venues_fragment) searchQuery.value
          else _bottomSheetVenueName.value.orEmpty()
      }
      addSource(searchQuery) {
        if (_bottomSheetDestinationId.value == R.id.venues_fragment) {
          value = searchQuery.value
        }
      }
      addSource(_bottomSheetVenueName) {
        if (_bottomSheetDestinationId.value != R.id.venues_fragment) {
          value = _bottomSheetVenueName.value
        }
      }
    }
  val searchBarQuery: LiveData<String> = _searchBarQuery

  private val _searchBarEnabled =
    MediatorLiveData<Boolean>().apply {
      addSource(_bottomSheetDestinationId) { value = it == R.id.venues_fragment }
    }
  val searchBarEnabled: LiveData<Boolean> = _searchBarEnabled

  private val _snackbarMessage = LiveEvent<Message>()
  val snackbarMessage: LiveData<Message> = _snackbarMessage

  private val _categoriesUpdated = LiveEvent<Unit>()
  val categoriesUpdated: LiveData<Unit> = _categoriesUpdated

  private val _venueClicked = LiveEvent<Venue>()
  val venueClicked: LiveData<Venue> = _venueClicked

  private val searchQueryObserver =
    Observer<String> { query ->
      sendVenueQueryUseCase(getTrimmedQueryOrEmptyUseCase(query, MIN_QUERY_LENGTH))
    }

  private val _webViewScrollableUpwards = LiveEvent<Boolean>()
  val webViewScrollableUpwards: LiveData<Boolean> = _webViewScrollableUpwards

  private val _isLocationLoading = MutableLiveData<Boolean>()
  val isLocationLoading: LiveData<Boolean> = _isLocationLoading

  private val _goToAppSettings = LiveEvent<Unit>()
  val goToAppSettings: LiveData<Unit> = _goToAppSettings

  init {
    receiveMessageUseCase()
      .subscribeBy(onNext = _snackbarMessage::setValue)
      .addTo(compositeDisposable)

    receiveCategoriesListLayoutEventUseCase()
      .subscribeBy(onNext = _categoriesUpdated::setValue)
      .addTo(compositeDisposable)

    receiveVenueClickedEventUseCase()
      .subscribeBy(onNext = _venueClicked::setValue)
      .addTo(compositeDisposable)

    receiveWebViewScrollableUpwardsUseCase()
      .subscribeBy(onNext = _webViewScrollableUpwards::setValue)
      .addTo(compositeDisposable)

    searchQuery.observeForever(searchQueryObserver)
  }

  fun onSearchViewsSizeMeasure(@BottomSheetBehavior.State sheetState: Int) {
    sendSheetSlideOffsetUseCase(if (sheetState == BottomSheetBehavior.STATE_EXPANDED) 1f else 0f)
  }

  fun onSheetSlide(slideOffset: Float) {
    sendSheetSlideOffsetUseCase(slideOffset)
  }

  fun onBottomSheetFragmentChanged(destinationId: Int, venueName: String?) {
    _bottomSheetDestinationId.value = destinationId
    _bottomSheetVenueName.value = venueName
  }

  fun onLocationPermissionGranted() {
    if (_isLocationLoading.value == true) return

    _isLocationLoading.value = true

    getCurrentUserLocationUseCase()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(
        onError = {
          Timber.e(it)
          sendMessageUseCase(
            Message.Shown(
              textResId = R.string.location_error,
              length = Message.Length.LONG,
              action = Message.Action(R.string.try_again, ::onLocationPermissionGranted)
            )
          )
        },
        onComplete = { _isLocationLoading.value = false },
        onSuccess = {
          sendUserLocationUseCase.invoke(it)
          _isLocationLoading.value = false
        }
      )
      .addTo(compositeDisposable)
  }

  fun onLocationPermissionDenied() {
    sendMessageUseCase(
      Message.Shown(
        textResId = R.string.location_permission_denied,
        length = Message.Length.LONG,
        action = Message.Action(R.string.settings) { _goToAppSettings.value = Unit }
      )
    )
  }

  override fun onCleared() {
    super.onCleared()
    searchQuery.removeObserver(searchQueryObserver)
    compositeDisposable.clear()
  }

  companion object {
    private const val MIN_QUERY_LENGTH = 1
  }
}
