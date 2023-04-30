package com.trm.opencoinmap.core.common.di

import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.usecase.ReceiveMapBoundsUseCase
import com.trm.opencoinmap.core.domain.usecase.SendMapBoundsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MapBoundsRelayModule {
  private val relay = PublishRelay.create<MapBounds>()

  @Provides fun sendMapBoundsUseCase() = SendMapBoundsUseCase(relay::accept)
  @Provides fun receiveMapBoundsUseCase() = ReceiveMapBoundsUseCase { relay }
}
