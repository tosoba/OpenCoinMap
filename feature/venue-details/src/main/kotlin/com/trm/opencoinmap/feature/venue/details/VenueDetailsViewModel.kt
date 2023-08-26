package com.trm.opencoinmap.feature.venue.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.trm.opencoinmap.core.domain.model.VenueDetails
import com.trm.opencoinmap.core.domain.usecase.GetVenueDetailsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveSheetSlideOffsetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
internal class VenueDetailsViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  private val getVenueDetailsUseCase: GetVenueDetailsUseCase,
  receiveSheetSlideOffsetUseCase: ReceiveSheetSlideOffsetUseCase,
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _viewState = MutableLiveData<ViewState>(ViewState.Loading)
  val viewState: LiveData<ViewState> = _viewState

  private val _sheetSlideOffset = LiveEvent<Float>()
  val sheetSlideOffset: LiveData<Float> = _sheetSlideOffset

  init {
    getVenueDetails()

    receiveSheetSlideOffsetUseCase()
      .filter { offset -> offset >= 0f }
      .subscribeBy(onNext = _sheetSlideOffset::setValue)
      .addTo(compositeDisposable)
  }

  fun onRetryClick() {
    getVenueDetails()
  }

  private fun getVenueDetails() {
    getVenueDetailsUseCase(requireNotNull(savedStateHandle[VenueDetailsArgs.VENUE_ID_KEY]))
      .map<ViewState>(ViewState::Loaded)
      .startWith(Maybe.just(ViewState.Loading))
      .defaultIfEmpty(ViewState.NotFound)
      .doOnError { Timber.e(it) }
      .onErrorReturn { ViewState.Error }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(onNext = _viewState::setValue)
      .addTo(compositeDisposable)
  }

  sealed interface ViewState {
    object Loading : ViewState

    data class Loaded(val venueDetails: VenueDetails) : ViewState {
      val actionsScrollViewVisible: Boolean
        get() =
          phoneVisible || emailVisible || facebookVisible || twitterVisible || instagramVisible

      val phoneVisible: Boolean
        get() = !venueDetails.phone.isNullOrBlank()

      val emailVisible: Boolean
        get() = !venueDetails.email.isNullOrBlank()

      val facebookVisible: Boolean
        get() = !venueDetails.facebook.isNullOrBlank()

      val twitterVisible: Boolean
        get() = !venueDetails.twitter.isNullOrBlank()

      val instagramVisible: Boolean
        get() = !venueDetails.instagram.isNullOrBlank()
    }

    object Error : ViewState

    object NotFound : ViewState
  }
}
