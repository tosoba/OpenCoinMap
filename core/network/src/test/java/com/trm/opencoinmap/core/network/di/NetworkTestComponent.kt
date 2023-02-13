package com.trm.opencoinmap.core.network.di

import com.trm.opencoinmap.core.network.retrofit.CoinMapApi
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface NetworkTestComponent {
  fun coinMapApi(): CoinMapApi
}
