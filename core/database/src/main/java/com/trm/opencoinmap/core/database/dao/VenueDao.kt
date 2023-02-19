package com.trm.opencoinmap.core.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.trm.opencoinmap.core.database.entity.VenueEntity

@Dao
interface VenueDao {
  @Upsert fun upsert(entity: VenueEntity)
}
