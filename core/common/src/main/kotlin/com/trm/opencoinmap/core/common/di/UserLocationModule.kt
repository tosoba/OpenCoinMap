package com.trm.opencoinmap.core.common.di

import android.content.Context
import com.jakewharton.rxrelay3.BehaviorRelay
import com.trm.opencoinmap.core.common.ext.getCurrentUserLocation
import com.trm.opencoinmap.core.domain.model.LatLng
import com.trm.opencoinmap.core.domain.usecase.GetCurrentUserLocationUseCase
import com.trm.opencoinmap.core.domain.usecase.ReceiveUserLocationUseCase
import com.trm.opencoinmap.core.domain.usecase.SendUserLocationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UserLocationModule {
  @Provides
  fun getCurrentUserLocationUseCase(@ApplicationContext context: Context) =
    GetCurrentUserLocationUseCase {
      context.getCurrentUserLocation().map {
        LatLng(latitude = it.latitude, longitude = it.longitude)
      }
    }

  private val relay = BehaviorRelay.create<LatLng>()

  @Provides fun sendUserLocationUseCase() = SendUserLocationUseCase(relay::accept)
  @Provides fun receiveUserLocationUseCase() = ReceiveUserLocationUseCase { relay }
}
