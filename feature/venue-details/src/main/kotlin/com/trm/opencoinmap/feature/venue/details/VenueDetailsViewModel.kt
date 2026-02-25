package com.trm.opencoinmap.feature.venue.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.common.util.LiveEvent
import com.trm.opencoinmap.core.domain.model.VenueDetails
import com.trm.opencoinmap.core.domain.usecase.GetVenueDetailsUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveSheetSlideOffsetUseCase
import com.trm.opencoinmap.core.domain.usecase.SendWebViewScrollableUpwardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class VenueDetailsViewModel
@Inject
constructor(
  private val savedStateHandle: SavedStateHandle,
  private val getVenueDetailsUseCase: GetVenueDetailsUseCase,
  receiveSheetSlideOffsetUseCase: ReceiveSheetSlideOffsetUseCase,
  private val sendWebViewScrollableUpwardsUseCase: SendWebViewScrollableUpwardsUseCase,
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _viewState = MutableLiveData<ViewState>(ViewState.Loading)
  val viewState: LiveData<ViewState> = _viewState

  private val _viewEvent = LiveEvent<ViewEvent>()
  val viewEvent: LiveData<ViewEvent> = _viewEvent

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

  fun onWebViewScrolled(isScrollableUpwards: Boolean) {
    sendWebViewScrollableUpwardsUseCase(isScrollableUpwards)
  }

  fun actionChips(viewState: ViewState.Loaded): List<VenueDetailsChipAction> = buildList {
    val (venueDetails) = viewState

    viewState.websiteUrl?.let { url ->
      add(
        VenueDetailsChipAction(R.string.open_in_browser, R.drawable.browser) {
          _viewEvent.value = ViewEvent.OpenInBrowser(url)
        }
      )
    }

    if (venueDetails.run { lat != null && lon != null }) {
      add(
        VenueDetailsChipAction(R.string.navigate, R.drawable.navigation) {
          _viewEvent.value =
            ViewEvent.Navigate(requireNotNull(venueDetails.lat), requireNotNull(venueDetails.lon))
        }
      )
    }

    venueDetails.phone?.takeIf(String::isNotBlank)?.let { number ->
      add(
        VenueDetailsChipAction(R.string.call, R.drawable.phone) {
          _viewEvent.value = ViewEvent.Dial(number)
        }
      )
    }

    venueDetails.email?.takeIf(String::isNotBlank)?.let { email ->
      VenueDetailsChipAction(R.string.email, R.drawable.email) {
        _viewEvent.value = ViewEvent.Mail(email)
      }
    }

    venueDetails.facebook?.takeIf(String::isNotBlank)?.let { facebook ->
      VenueDetailsChipAction(R.string.facebook, R.drawable.facebook) {
        _viewEvent.value = ViewEvent.Facebook(facebook)
      }
    }

    venueDetails.twitter?.takeIf(String::isNotBlank)?.let { twitter ->
      VenueDetailsChipAction(R.string.twitter, R.drawable.twitter) {
        _viewEvent.value = ViewEvent.Twitter(twitter)
      }
    }

    venueDetails.instagram?.takeIf(String::isNotBlank)?.let { instagram ->
      VenueDetailsChipAction(R.string.instagram, R.drawable.instagram) {
        _viewEvent.value = ViewEvent.Instagram(instagram)
      }
    }
  }

  sealed interface ViewState {
    object Loading : ViewState

    data class Loaded(val venueDetails: VenueDetails) : ViewState {
      val websiteUrl: String?
        get() = venueDetails.website?.takeIf(String::isNotBlank)?.replace("http:", "https:")
    }

    object Error : ViewState

    object NotFound : ViewState
  }

  sealed interface ViewEvent {
    data class OpenInBrowser(val url: String) : ViewEvent

    data class Navigate(val lat: Double, val lon: Double) : ViewEvent

    data class Dial(val number: String) : ViewEvent

    data class Mail(val address: String) : ViewEvent

    data class Facebook(val name: String) : ViewEvent

    data class Twitter(val name: String) : ViewEvent

    data class Instagram(val name: String) : ViewEvent
  }
}
