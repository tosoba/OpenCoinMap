package com.trm.opencoinmap.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object SchedulersModule {
  @Provides @IoScheduler fun io(): Scheduler = Schedulers.io()
  @Provides @MainScheduler fun main(): Scheduler = AndroidSchedulers.mainThread()
}

@Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class IoScheduler

@Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class MainScheduler
