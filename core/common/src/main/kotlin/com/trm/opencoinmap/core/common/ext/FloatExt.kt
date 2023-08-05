package com.trm.opencoinmap.core.common.ext

import android.content.Context

fun Float.toPx(context: Context): Float = this * context.resources.displayMetrics.density

fun Float.toDp(context: Context): Float = this / context.resources.displayMetrics.density
