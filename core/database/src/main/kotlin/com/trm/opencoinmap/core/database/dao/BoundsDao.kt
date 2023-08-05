package com.trm.opencoinmap.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trm.opencoinmap.core.database.entity.BoundsEntity

@Dao
interface BoundsDao {
  @Upsert fun upsert(entity: BoundsEntity)

  @Query("DELETE FROM bounds WHERE whole = FALSE") fun deleteNonWhole()
}
