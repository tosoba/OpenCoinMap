package com.trm.opencoinmap.core.common.ext

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap

fun Context.lazyBitmapResource(@DrawableRes drawableId: Int): Lazy<Bitmap> =
  lazy(LazyThreadSafetyMode.NONE) {
    requireNotNull(ContextCompat.getDrawable(this, drawableId)).toBitmap()
  }

fun Float.toPx(context: Context): Float = this * context.resources.displayMetrics.density

fun Float.toDp(context: Context): Float = this / context.resources.displayMetrics.density
