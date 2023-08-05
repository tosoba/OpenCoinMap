package com.trm.opencoinmap.core.common.ext

import android.content.res.Configuration
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun Configuration.calculateLatLonDivisors(): Pair<Int, Int> {
  val widthDp = screenWidthDp.toDouble()
  val heightDp = screenHeightDp.toDouble()
  val multiplier = (max(widthDp, heightDp) / min(widthDp, heightDp)).roundToInt()
  val smallerDivisor = 3
  val largerDivisor = smallerDivisor * multiplier
  val latDivisor = if (heightDp > widthDp) largerDivisor else smallerDivisor
  val lonDivisor = if (widthDp > heightDp) largerDivisor else smallerDivisor
  return latDivisor to lonDivisor
}
