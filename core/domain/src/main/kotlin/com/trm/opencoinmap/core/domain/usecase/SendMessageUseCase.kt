package com.trm.opencoinmap.core.domain.usecase

import com.trm.opencoinmap.core.domain.model.Message

fun interface SendMessageUseCase {
  operator fun invoke(message: Message)
}
