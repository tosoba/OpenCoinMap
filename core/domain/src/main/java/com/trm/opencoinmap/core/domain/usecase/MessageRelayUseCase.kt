package com.trm.opencoinmap.core.domain.usecase

import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.domain.model.Message
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRelayUseCase @Inject constructor() {
  private val messageSubject = PublishRelay.create<Message>()

  fun accept(message: Message) {
    messageSubject.accept(message)
  }

  fun observable(): Observable<Message> = messageSubject
}
