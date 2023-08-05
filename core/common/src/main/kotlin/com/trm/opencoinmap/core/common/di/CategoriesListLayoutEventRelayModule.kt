package com.trm.opencoinmap.core.common.di

import com.jakewharton.rxrelay3.PublishRelay
import com.trm.opencoinmap.core.domain.usecase.ReceiveCategoriesListLayoutEventUseCase
import com.trm.opencoinmap.core.domain.usecase.SendCategoriesListLayoutEventUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CategoriesListLayoutEventRelayModule {
  private val relay = PublishRelay.create<Unit>()

  @Provides
  fun sendCategoriesListLayoutEventUseCase() = SendCategoriesListLayoutEventUseCase {
    relay.accept(Unit)
  }

  @Provides
  fun receiveCategoriesListLayoutEventUseCase() = ReceiveCategoriesListLayoutEventUseCase { relay }
}
