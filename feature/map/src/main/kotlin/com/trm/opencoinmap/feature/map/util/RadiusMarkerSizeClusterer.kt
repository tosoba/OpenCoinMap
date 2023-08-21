package com.trm.opencoinmap.feature.map.util

import android.content.Context
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
    Marker(mapView).apply {
      position = cluster.position
      setInfoWindow(null)
      setAnchor(mAnchorU, mAnchorV)
      icon = iconBuilder.build(cluster.size)
      if (cluster.size > 1) {
        repeat(cluster.size) { getItem(it).setOnMarkerClickListener(null) }
      } else if (cluster.size == 0) {
        getItem(0).setOnMarkerClickListener { marker, mapView ->
          // TODO: figure out a way to add on marker click
          true
        }
      }
    }

  override fun zoomOnCluster(mapView: MapView, cluster: StaticCluster) {
    mapView.animateTo(cluster.boundingBox)
  }
}
