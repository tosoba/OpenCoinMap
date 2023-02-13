package com.trm.opencoinmap.core.network.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.trm.opencoinmap.core.common.BuildConfig
import com.trm.opencoinmap.core.network.retrofit.CoinMapApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import javax.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
  @Provides @Singleton fun networkJson(): Json = Json { ignoreUnknownKeys = true }

  @Provides
  @Singleton
  fun coinMapApi(networkJson: Json): CoinMapApi =
    Retrofit.Builder()
      .client(
        OkHttpClient.Builder()
          .addInterceptor(
            HttpLoggingInterceptor().apply {
              if (BuildConfig.DEBUG) setLevel(HttpLoggingInterceptor.Level.BODY)
            }
          )
          .build()
      )
      .addConverterFactory(
        @OptIn(ExperimentalSerializationApi::class)
        networkJson.asConverterFactory("application/json".toMediaType())
      )
      .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
      .baseUrl(CoinMapApi.BASE_URL)
      .build()
      .create(CoinMapApi::class.java)
}
