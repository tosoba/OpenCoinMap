package com.trm.opencoinmap.feature.map.util

import android.view.MotionEvent
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class NonDefaultClickableMarker(mapView: MapView) : Marker(mapView) {
  override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean =
    if (hitTest(event, mapView)) {
      if (mOnMarkerClickListener == null) {
        true
      } else {
        mOnMarkerClickListener.onMarkerClick(this, mapView)
      }
    } else {
      false
    }
}
