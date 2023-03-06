package com.trm.opencoinmap

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.trm.opencoinmap.core.domain.model.Message
import com.trm.opencoinmap.core.domain.usecase.MessageSubjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject
constructor(
  messageSubjectUseCase: MessageSubjectUseCase,
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _snackbarMessage: LiveEvent<Message> = LiveEvent()
  val snackbarMessage: LiveData<Message> = _snackbarMessage

  init {
    messageSubjectUseCase
      .observable()
      .subscribeBy(onNext = _snackbarMessage::setValue)
      .addTo(compositeDisposable)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
