package com.trm.opencoinmap.feature.map.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.trm.opencoinmap.feature.map.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.osmdroid.views.overlay.Marker

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

  val bitmap: Bitmap by
    lazy(LazyThreadSafetyMode.NONE) {
      requireNotNull(ContextCompat.getDrawable(context, R.drawable.cluster_marker)).toBitmap()
    }

  @MainThread
  fun build(size: Int): BitmapDrawable {
    val densityDpi = context.resources.displayMetrics.densityDpi
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
}
