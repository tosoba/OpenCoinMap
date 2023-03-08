package com.trm.opencoinmap.feature.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.common.R as commonR
import com.trm.opencoinmap.core.common.view.get
import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.core.domain.usecase.MarkersInBoundsSubjectUseCase
import com.trm.opencoinmap.core.domain.usecase.MessageSubjectUseCase
import com.trm.opencoinmap.feature.map.model.BoundingBoxArgs
import com.trm.opencoinmap.feature.map.model.MapPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
internal class MapViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val markersInBoundsSubjectUseCase: MarkersInBoundsSubjectUseCase,
  private val messageSubjectUseCase: MessageSubjectUseCase,
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  var mapPosition: MapPosition by savedStateHandle.get(defaultValue = MapPosition())
  var latestBoundingBoxArgs: BoundingBoxArgs? = null

  private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
  val isLoading: LiveData<Boolean> = _isLoading

  private val _markersInBounds: MutableLiveData<List<MapMarker>> = MutableLiveData(emptyList())
  val markersInBounds: LiveData<List<MapMarker>> = _markersInBounds

  init {
    markersInBoundsSubjectUseCase
      .observable()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeBy(
        onNext = {
          _isLoading.value = it is Loading
          if (it is WithData) _markersInBounds.value = it.data
          sendMessage(it)
        },
        onError = { Timber.tag(TAG).e(it) }
      )
      .addTo(compositeDisposable)
  }

  private fun sendMessage(loadable: Loadable<List<MapMarker>>) {
    messageSubjectUseCase.onNext(
      if (loadable is Failed) {
        Message.Shown(
          textResId = commonR.string.error_occurred,
          length = Message.Length.LONG,
          action =
            Message.Action(commonR.string.retry) { latestBoundingBoxArgs?.let(::onBoundingBox) }
        )
      } else {
        Message.Hidden
      }
    )
  }

  fun onBoundingBox(args: BoundingBoxArgs) {
    latestBoundingBoxArgs = args
    val (boundingBox, latDivisor, lonDivisor) = args
    messageSubjectUseCase.onNext(Message.Hidden)
    markersInBoundsSubjectUseCase.onNext(
      MarkersInBoundsSubjectUseCase.Args(
        minLat = boundingBox.latSouth,
        maxLat = boundingBox.latNorth,
        minLon = boundingBox.lonWest,
        maxLon = boundingBox.lonEast,
        latDivisor = latDivisor,
        lonDivisor = lonDivisor
      )
    )
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

  companion object {
    private const val TAG = "MAP_VM"
  }
}
