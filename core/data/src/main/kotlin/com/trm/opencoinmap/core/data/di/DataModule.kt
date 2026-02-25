package com.trm.opencoinmap.core.data.di

import com.trm.opencoinmap.core.data.repo.VenueRepoImpl
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
  @Binds fun bind(impl: VenueRepoImpl): VenueRepo
}
