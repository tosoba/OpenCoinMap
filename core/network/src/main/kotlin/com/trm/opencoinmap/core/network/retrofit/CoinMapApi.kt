package com.trm.opencoinmap.core.network.retrofit

import com.trm.opencoinmap.core.network.model.VenueResponse
import com.trm.opencoinmap.core.network.model.VenuesResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinMapApi {
  @GET("/api/v1/venues")
  fun getVenues(
    @Query("lat1") minLat: Double? = null,
    @Query("lat2") maxLat: Double? = null,
    @Query("lon1") minLon: Double? = null,
    @Query("lon2") maxLon: Double? = null,
    @Query("query") query: String? = null,
    @Query("category") category: String? = null,
    @Query("limit") limit: Int? = null,
    @Query("offset") offset: Int? = null,
  ): Single<VenuesResponse>

  @GET("/api/v1/venues/{id}") fun getVenue(@Path("id") id: Int): Single<VenueResponse>

  companion object {
    internal const val BASE_URL = "https://coinmap.org/"
  }
}
