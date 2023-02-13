package com.trm.opencoinmap.core.network.retrofit

import com.trm.opencoinmap.core.network.model.VenuesResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET

interface CoinMapApi {
  @GET("/api/v1/venues") fun getVenues(): Single<VenuesResponse>

  companion object {
    internal const val BASE_URL = "https://coinmap.org/"
  }
}
