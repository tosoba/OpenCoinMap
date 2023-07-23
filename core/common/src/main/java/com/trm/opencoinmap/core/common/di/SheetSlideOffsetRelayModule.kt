package com.trm.opencoinmap.core.common.di

import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.domain.usecase.ReceiveSheetSlideOffsetUseCase
import com.trm.opencoinmap.core.domain.usecase.SendSheetSlideOffsetUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.core.BackpressureStrategy

@Module
@InstallIn(SingletonComponent::class)
object SheetSlideOffsetRelayModule {
  private val relay = PublishRelay.create<Float>()

  @Provides fun sendSheetSlideOffsetEventUseCase() = SendSheetSlideOffsetUseCase(relay::accept)

  @Provides
  fun receiveSheetSlideOffsetEventUseCase() = ReceiveSheetSlideOffsetUseCase {
    relay.toFlowable(BackpressureStrategy.LATEST)
  }
}
