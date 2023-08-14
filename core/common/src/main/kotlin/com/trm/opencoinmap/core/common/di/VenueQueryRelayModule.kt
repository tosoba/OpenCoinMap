package com.trm.opencoinmap.core.common.di

import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.domain.usecase.ReceiveVenueQueryUseCase
import com.trm.opencoinmap.core.domain.usecase.SendVenueQueryUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object VenueQueryRelayModule {
  private val relay = PublishRelay.create<String>()

  @Provides fun sendVenueQueryUseCase() = SendVenueQueryUseCase(relay::accept)
  @Provides fun receiveVenueQueryUseCase() = ReceiveVenueQueryUseCase { relay }
}
