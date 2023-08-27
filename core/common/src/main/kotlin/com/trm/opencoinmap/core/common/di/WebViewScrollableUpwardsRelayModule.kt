package com.trm.opencoinmap.core.common.di

import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.domain.usecase.ReceiveWebViewScrollableUpwardsUseCase
import com.trm.opencoinmap.core.domain.usecase.SendWebViewScrollableUpwardsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.core.BackpressureStrategy

@Module
@InstallIn(SingletonComponent::class)
object WebViewScrollableUpwardsRelayModule {
  private val relay = PublishRelay.create<Boolean>()

  @Provides
  fun sendWebViewScrollableUpwardsUseCase() = SendWebViewScrollableUpwardsUseCase(relay::accept)

  @Provides
  fun receiveWebViewScrollableUpwardsUseCase() = ReceiveWebViewScrollableUpwardsUseCase {
    relay.toFlowable(BackpressureStrategy.LATEST)
  }
}
