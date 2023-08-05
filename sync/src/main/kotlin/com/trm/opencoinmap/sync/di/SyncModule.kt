package com.trm.opencoinmap.sync.di

import com.trm.opencoinmap.core.domain.sync.SyncDataSource
import com.trm.opencoinmap.sync.SyncLocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface SyncModule {
  @Binds fun SyncLocalDataSource.bind(): SyncDataSource
}
