package com.trm.opencoinmap.core.datastore.di

import com.trm.opencoinmap.core.datastore.PreferenceDataSourceImpl
import com.trm.opencoinmap.core.domain.repo.PreferenceDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataStoreModule {
  @Binds fun PreferenceDataSourceImpl.preferenceDataStore(): PreferenceDataSource
}
