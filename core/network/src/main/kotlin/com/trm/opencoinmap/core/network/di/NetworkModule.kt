package com.trm.opencoinmap.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trm.opencoinmap.core.common.BuildConfig
import com.trm.opencoinmap.core.network.retrofit.BtcMapApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides @Singleton fun networkJson(): Json = Json { ignoreUnknownKeys = true }

  @Provides
  @Singleton
  fun btcMapApi(networkJson: Json): BtcMapApi =
    Retrofit.Builder()
      .client(
        OkHttpClient.Builder()
          .addInterceptor(
            HttpLoggingInterceptor().apply {
              if (BuildConfig.DEBUG) setLevel(HttpLoggingInterceptor.Level.BASIC)
            }
          )
          .build()
      )
      .addConverterFactory(
        @OptIn(ExperimentalSerializationApi::class)
        networkJson.asConverterFactory("application/json".toMediaType())
      )
      .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
      .baseUrl(BtcMapApi.BASE_URL)
      .build()
      .create(BtcMapApi::class.java)
}
