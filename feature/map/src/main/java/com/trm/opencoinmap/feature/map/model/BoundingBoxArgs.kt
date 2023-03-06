package com.trm.opencoinmap.feature.map.model

import org.osmdroid.util.BoundingBox

data class BoundingBoxArgs(val boundingBox: BoundingBox, val latDivisor: Int, val lonDivisor: Int)
