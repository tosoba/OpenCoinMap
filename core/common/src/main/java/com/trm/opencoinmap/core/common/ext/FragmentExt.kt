package com.trm.opencoinmap.core.common.ext

import androidx.fragment.app.Fragment
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun Fragment.calculateLatLonDivisors(): Pair<Int, Int> {
  val widthDp = resources.configuration.screenWidthDp.toDouble()
  val heightDp = resources.configuration.screenHeightDp.toDouble()
  val multiplier = (max(widthDp, heightDp) / min(widthDp, heightDp)).roundToInt()
  val smallerDivisor = 3
  val largerDivisor = smallerDivisor * multiplier
  val latDivisor = if (heightDp > widthDp) largerDivisor else smallerDivisor
  val lonDivisor = if (widthDp > heightDp) largerDivisor else smallerDivisor
  return latDivisor to lonDivisor
}
