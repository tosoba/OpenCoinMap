package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.Message
import io.reactivex.rxjava3.core.Observable

fun interface ReceiveMessageUseCase {
  operator fun invoke(): Observable<Message>
}
