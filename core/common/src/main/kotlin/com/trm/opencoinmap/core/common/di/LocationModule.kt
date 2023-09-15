package com.trm.opencoinmap.core.common.di

import android.content.Context
import com.trm.opencoinmap.core.common.ext.getCurrentUserLocation
import com.trm.opencoinmap.core.domain.model.LatLng
import com.trm.opencoinmap.core.domain.usecase.GetCurrentUserLocationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {
  @Provides
  fun getCurrentUserLocationUseCase(@ApplicationContext context: Context) =
    GetCurrentUserLocationUseCase {
      context.getCurrentUserLocation().map {
        LatLng(latitude = it.latitude, longitude = it.longitude)
      }
    }
}
