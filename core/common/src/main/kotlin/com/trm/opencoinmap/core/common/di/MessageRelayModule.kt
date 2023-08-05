package com.trm.opencoinmap.core.common.di

import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.domain.model.Message
import com.trm.opencoinmap.core.domain.usecase.ReceiveMessageUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMessageUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MessageRelayModule {
  private val relay = PublishRelay.create<Message>()

  @Provides fun sendMessageUseCase() = SendMessageUseCase(relay::accept)
  @Provides fun receiveMessageUseCase() = ReceiveMessageUseCase { relay }
}
