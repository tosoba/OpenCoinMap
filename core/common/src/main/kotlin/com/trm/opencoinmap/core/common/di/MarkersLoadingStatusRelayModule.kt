package com.trm.opencoinmap.core.common.di

import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.domain.model.MarkersLoadingStatus
import com.trm.opencoinmap.core.domain.usecase.ReceiveMarkersLoadingStatusUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMarkersLoadingStatusUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MarkersLoadingStatusRelayModule {
  private val relay = PublishRelay.create<MarkersLoadingStatus>()

  @Provides fun sendMarkersLoadingStatusUseCase() = SendMarkersLoadingStatusUseCase(relay::accept)
  @Provides fun receiveMarkersLoadingStatusUseCase() = ReceiveMarkersLoadingStatusUseCase { relay }
}
