package com.trm.opencoinmap.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.trm.opencoinmap.core.database.converter.StringListConverter
import com.trm.opencoinmap.core.database.dao.BoundsDao
import com.trm.opencoinmap.core.database.dao.VenueDao
import com.trm.opencoinmap.core.database.dao.VenueDetailsDao
import com.trm.opencoinmap.core.database.entity.BoundsEntity
import com.trm.opencoinmap.core.database.entity.VenueDetailsEntity
import com.trm.opencoinmap.core.database.entity.VenueEntity

@Database(
  entities = [VenueEntity::class, VenueDetailsEntity::class, BoundsEntity::class],
  version = 1
)
@TypeConverters(StringListConverter::class)
abstract class OpenCoinMapDatabase : RoomDatabase() {
  abstract fun venueDao(): VenueDao
  abstract fun venueDetailsDao(): VenueDetailsDao
  abstract fun boundsDao(): BoundsDao

  internal companion object {
    fun build(context: Context): OpenCoinMapDatabase =
      Room.databaseBuilder(context, OpenCoinMapDatabase::class.java, "open_coin_map.db").build()
  }
}
