package com.trm.opencoinmap.core.network.retrofit

import com.trm.opencoinmap.core.network.di.DaggerNetworkTestComponent
import org.junit.Test

class CoinMapApiTests {
  private val api: CoinMapApi = DaggerNetworkTestComponent.builder().build().coinMapApi()

  @Test
  fun getVenues() {
    api.getVenues().blockingGet().venues
  }

  @Test
  fun getVenue() {
    println(api.getVenue(1).blockingGet())
  }
}
