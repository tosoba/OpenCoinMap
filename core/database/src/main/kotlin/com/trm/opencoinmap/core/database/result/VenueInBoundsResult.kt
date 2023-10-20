package com.trm.opencoinmap.core.database.result

import androidx.room.Embedded
import com.trm.opencoinmap.core.database.entity.VenueEntity

data class VenueInBoundsResult(@Embedded val venue: VenueEntity, val index: Int)
