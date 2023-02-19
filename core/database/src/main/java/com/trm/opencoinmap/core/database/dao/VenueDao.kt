package com.trm.opencoinmap.core.database.dao

import androidx.room.Dao
import androidx.room.Upsert
import com.trm.opencoinmap.core.database.entity.VenueEntity
import io.reactivex.rxjava3.core.Completable

@Dao
interface VenueDao {
  @Upsert fun upsert(entities: List<VenueEntity>): Completable
}
