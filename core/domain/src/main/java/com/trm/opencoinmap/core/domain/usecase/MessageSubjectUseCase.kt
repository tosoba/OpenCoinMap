package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.Message
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageSubjectUseCase @Inject constructor() {
  private val messageSubject = PublishSubject.create<Message>()

  fun onNext(message: Message) {
    messageSubject.onNext(message)
  }

  fun observable(): Observable<Message> = messageSubject
}
