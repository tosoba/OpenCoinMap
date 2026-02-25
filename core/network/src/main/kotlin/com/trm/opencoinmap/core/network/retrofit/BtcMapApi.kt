package com.trm.opencoinmap.core.network.retrofit

import com.trm.opencoinmap.core.network.model.BtcMapPlace
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BtcMapApi {
  @GET("/v4/places")
  fun getPlaces(
    @Query("updated_since") updatedSince: String? = null,
    @Query("limit") limit: Int? = null,
    @Query("fields") fields: String? = "id,lat,lon,icon,name,created_at",
  ): Single<List<BtcMapPlace>>

  @GET("/v4/places/search")
  fun searchPlaces(
    @Query("lat") lat: Double? = null,
    @Query("lon") lon: Double? = null,
    @Query("radius_km") radiusKm: Double? = null,
    @Query("name") name: String? = null,
  ): Single<List<BtcMapPlace>>

  @GET("/v4/places/{id}")
  fun getPlace(
    @Path("id") id: Long,
    @Query("fields")
    fields: String? =
      "id,name,lat,lon,icon,address,phone,website,email,facebook,instagram,twitter,description,opening_hours,created_at,updated_at",
  ): Single<BtcMapPlace>

  companion object {
    const val BASE_URL = "https://api.btcmap.org/"
  }
}
