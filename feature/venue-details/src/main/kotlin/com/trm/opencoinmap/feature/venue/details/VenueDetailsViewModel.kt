package com.trm.opencoinmap.feature.venue.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.domain.model.VenueDetails
import com.trm.opencoinmap.core.domain.usecase.GetVenueDetailsUseCase
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
  private val getVenueDetailsUseCase: GetVenueDetailsUseCase
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _viewState = MutableLiveData<ViewState>(ViewState.Loading)
  val viewState: LiveData<ViewState> = _viewState

  init {
    getVenueDetails()
  }

  fun onRetryClick() {
    getVenueDetails()
  }

  private fun getVenueDetails() {
    getVenueDetailsUseCase(requireNotNull(savedStateHandle[VenueDetailsArgs.VENUE_ID_KEY]))
      .map { if (it.website != null) ViewState.Loaded(it) else ViewState.WebsiteMissing(it) }
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
    data class Loaded(val venueDetails: VenueDetails) : ViewState
    object Error : ViewState
    object NotFound : ViewState
    data class WebsiteMissing(val venueDetails: VenueDetails) : ViewState
  }
}
