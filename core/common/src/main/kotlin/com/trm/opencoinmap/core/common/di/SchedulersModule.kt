package com.trm.opencoinmap.core.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
interface SchedulersModule {
  @Binds fun AppRxSchedulers.bind(): RxSchedulers
}

interface RxSchedulers {
  val io: Scheduler
  val main: Scheduler
}

class AppRxSchedulers @Inject constructor() : RxSchedulers {
  override val io: Scheduler = Schedulers.io()

  override val main: Scheduler = AndroidSchedulers.mainThread()
}
