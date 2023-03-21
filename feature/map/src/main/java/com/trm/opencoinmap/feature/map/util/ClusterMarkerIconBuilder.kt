package com.trm.opencoinmap.feature.map.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.MainThread
import com.trm.opencoinmap.core.common.ext.lazyBitmapResource
import com.trm.opencoinmap.feature.map.R
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.FragmentScoped
import javax.inject.Inject
import org.osmdroid.views.overlay.Marker

@FragmentScoped
class ClusterMarkerIconBuilder
@Inject
constructor(@ApplicationContext private val context: Context) {
  private val whiteClusterSizeTextPaint: Paint by
    lazy(LazyThreadSafetyMode.NONE) {
      Paint().apply {
        color = Color.WHITE
        textSize = 15 * context.resources.displayMetrics.density
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
      }
    }

  private val bitmap1000: Bitmap by context.lazyBitmapResource(R.drawable.cluster_marker_1000)
  private val bitmap500: Bitmap by context.lazyBitmapResource(R.drawable.cluster_marker_500)
  private val bitmap250: Bitmap by context.lazyBitmapResource(R.drawable.cluster_marker_250)
  private val bitmap100: Bitmap by context.lazyBitmapResource(R.drawable.cluster_marker_100)
  private val bitmap1: Bitmap by context.lazyBitmapResource(R.drawable.cluster_marker_1)

  @MainThread
  fun build(size: Int): BitmapDrawable {
    val densityDpi = context.resources.displayMetrics.densityDpi
    val bitmap = getBitmapForSize(size)
    val icon =
      Bitmap.createBitmap(
        bitmap.getScaledWidth(densityDpi),
        bitmap.getScaledHeight(densityDpi),
        bitmap.config
      )
    val iconCanvas = Canvas(icon)
    iconCanvas.drawBitmap(bitmap, 0f, 0f, null)
    val textHeight =
      (whiteClusterSizeTextPaint.descent() + whiteClusterSizeTextPaint.ascent()).toInt()
    iconCanvas.drawText(
      size.toString(),
      Marker.ANCHOR_CENTER * icon.width,
      Marker.ANCHOR_CENTER * icon.height - textHeight / 2,
      whiteClusterSizeTextPaint
    )
    return BitmapDrawable(context.resources, icon)
  }

  private fun getBitmapForSize(size: Int): Bitmap =
    when {
      size > 1000 -> bitmap1000
      size > 500 -> bitmap500
      size > 250 -> bitmap250
      size > 100 -> bitmap100
      else -> bitmap1
    }
}
