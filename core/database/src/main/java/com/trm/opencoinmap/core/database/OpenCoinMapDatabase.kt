package com.trm.opencoinmap.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.trm.opencoinmap.core.database.dao.VenueDao
import com.trm.opencoinmap.core.database.entity.VenueEntity

@Database(entities = [VenueEntity::class], version = 1)
abstract class OpenCoinMapDatabase : RoomDatabase() {
  abstract fun venueDao(): VenueDao

  internal companion object {
    fun build(context: Context): OpenCoinMapDatabase =
      Room.databaseBuilder(context, OpenCoinMapDatabase::class.java, "open_coin_map.db").build()
  }
}
