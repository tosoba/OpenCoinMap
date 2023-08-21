package com.trm.opencoinmap.feature.map.util

import android.content.Context
import android.view.MotionEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.bonuspack.clustering.StaticCluster
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

internal class RadiusMarkerSizeClusterer
@Inject
constructor(
  @ApplicationContext context: Context,
  private val iconBuilder: ClusterMarkerIconBuilder,
) : RadiusMarkerClusterer(context) {
  override fun buildClusterMarker(cluster: StaticCluster, mapView: MapView): Marker =
    object : Marker(mapView) {
        override fun onSingleTapConfirmed(event: MotionEvent?, mapView: MapView?): Boolean {
          return if (hitTest(event, mapView)) {
            if (mOnMarkerClickListener == null) {
              true
            } else {
              mOnMarkerClickListener.onMarkerClick(this, mapView)
            }
          } else false
        }
      }
      .apply {
        position = cluster.position
        setInfoWindow(null)
        setAnchor(mAnchorU, mAnchorV)
        icon = iconBuilder.build(cluster.size)
      }

  override fun zoomOnCluster(mapView: MapView, cluster: StaticCluster) {
    mapView.animateTo(cluster.boundingBox)
  }
}
