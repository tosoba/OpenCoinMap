package com.trm.opencoinmap.core.database.di

import android.content.Context
import com.trm.opencoinmap.core.database.OpenCoinMapDatabase
import com.trm.opencoinmap.core.database.dao.BoundsDao
import com.trm.opencoinmap.core.database.dao.VenueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
  @Provides
  @Singleton
  fun openCoinMapDatabase(@ApplicationContext context: Context): OpenCoinMapDatabase =
    OpenCoinMapDatabase.build(context)

  @Provides fun venueDao(db: OpenCoinMapDatabase): VenueDao = db.venueDao()

  @Provides fun boundsDao(db: OpenCoinMapDatabase): BoundsDao = db.boundsDao()
}
