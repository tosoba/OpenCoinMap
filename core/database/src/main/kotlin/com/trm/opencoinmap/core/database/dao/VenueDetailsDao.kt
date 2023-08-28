package com.trm.opencoinmap.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trm.opencoinmap.core.database.entity.VenueDetailsEntity
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

@Dao
interface VenueDetailsDao {
  @Query("SELECT * FROM venue_details WHERE id = :id")
  fun selectById(id: Long): Maybe<VenueDetailsEntity>

  @Upsert fun upsert(entity: VenueDetailsEntity): Completable
}
