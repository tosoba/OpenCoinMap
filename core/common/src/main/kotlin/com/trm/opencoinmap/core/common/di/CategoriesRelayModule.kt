package com.trm.opencoinmap.core.common.di

import com.jakewharton.rxrelay3.BehaviorRelay
import com.trm.opencoinmap.core.domain.usecase.ReceiveCategoriesUseCase
import com.trm.opencoinmap.core.domain.usecase.SendCategoriesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CategoriesRelayModule {
  private val relay = BehaviorRelay.create<List<String>>()

  @Provides fun sendCategoriesUseCase() = SendCategoriesUseCase(relay::accept)
  @Provides fun receiveCategoriesUseCase() = ReceiveCategoriesUseCase { relay }
}
