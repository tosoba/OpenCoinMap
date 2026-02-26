package com.trm.opencoinmap.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.trm.opencoinmap.core.database.converter.StringListConverter
import com.trm.opencoinmap.core.database.dao.BoundsDao
import com.trm.opencoinmap.core.database.dao.VenueDao
import com.trm.opencoinmap.core.database.dao.VenueDetailsDao
import com.trm.opencoinmap.core.database.entity.BoundsEntity
import com.trm.opencoinmap.core.database.entity.VenueDetailsEntity
import com.trm.opencoinmap.core.database.entity.VenueEntity

@Database(
  entities = [VenueEntity::class, VenueDetailsEntity::class, BoundsEntity::class],
  version = 2,
)
@TypeConverters(StringListConverter::class)
abstract class OpenCoinMapDatabase : RoomDatabase() {
  abstract fun venueDao(): VenueDao

  abstract fun venueDetailsDao(): VenueDetailsDao

  abstract fun boundsDao(): BoundsDao

  internal companion object {
    private val MIGRATION_1_2 =
      object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
          db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `venue_details_new` (
              `category` TEXT,
              `created_on` INTEGER,
              `description` TEXT,
              `email` TEXT,
              `facebook` TEXT,
              `id` INTEGER NOT NULL,
              `instagram` TEXT,
              `lat` REAL,
              `logo_url` TEXT,
              `lon` REAL,
              `name` TEXT,
              `name_ascii` TEXT,
              `phone` TEXT,
              `src_id` TEXT,
              `street` TEXT,
              `twitter` TEXT,
              `updated_on` INTEGER,
              `website` TEXT,
              `inserted_at_timestamp` INTEGER NOT NULL,
              PRIMARY KEY(`id`)
            )
            """
              .trimIndent()
          )

          db.execSQL(
            """
            INSERT INTO `venue_details_new` (
              `category`, `created_on`, `description`, `email`, `facebook`, `id`, `instagram`,
              `lat`, `logo_url`, `lon`, `name`, `name_ascii`, `phone`, `src_id`, `street`,
              `twitter`, `updated_on`, `website`, `inserted_at_timestamp`
            )
            SELECT
              `category`, `created_on`, `description`, `email`, `facebook`, `id`, `instagram`,
              `lat`, `logo_url`, `lon`, `name`, `name_ascii`, `phone`, `src_id`, `street`,
              `twitter`, `updated_on`, `website`, `inserted_at_timestamp`
            FROM `venue_details`
            """
              .trimIndent()
          )

          db.execSQL("DROP TABLE `venue_details`")
          db.execSQL("ALTER TABLE `venue_details_new` RENAME TO `venue_details`")
        }
      }

    fun build(context: Context): OpenCoinMapDatabase =
      Room.databaseBuilder(context, OpenCoinMapDatabase::class.java, "open_coin_map.db")
        .addMigrations(MIGRATION_1_2)
        .build()
  }
}
