package com.trm.opencoinmap.core.common.di

import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.domain.usecase.ReceiveVenueClickedEventUseCase
import com.trm.opencoinmap.core.domain.usecase.SendVenueClickedEventUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object VenueClickedEventModule {
  private val relay = PublishRelay.create<Long>()

  @Provides fun sendVenueClickedEventUseCase() = SendVenueClickedEventUseCase(relay::accept)
  @Provides fun receiveVenueClickedEventUseCase() = ReceiveVenueClickedEventUseCase { relay }
}
