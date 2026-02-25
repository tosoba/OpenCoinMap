package com.trm.opencoinmap.core.data.ext

import com.trm.opencoinmap.core.network.model.BtcMapPlace

fun BtcMapPlace.isValid(): Boolean = id != 0L && lat != 0.0 && lon != 0.0
