package com.trm.opencoinmap.feature.map.util

import android.graphics.drawable.Drawable
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.feature.map.model.MapPosition
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

internal fun MapView.setDefaultConfig() {
  setTileSource(MapDefaults.tileSource)
  isTilesScaledToDpi = true
  setMultiTouchControls(true)
  zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
  isHorizontalMapRepetitionEnabled = false
  isVerticalMapRepetitionEnabled = false
  val tileSystem = MapView.getTileSystem()
  setScrollableAreaLimitLatitude(tileSystem.maxLatitude, tileSystem.minLatitude, 0)
  setScrollableAreaLimitLongitude(tileSystem.minLongitude, tileSystem.maxLongitude, 0)
  minZoomLevel = MapDefaults.MIN_ZOOM
}

internal fun MapView.restorePosition(position: MapPosition) {
  controller.setZoom(position.zoom)
  mapOrientation = position.orientation
  setExpectedCenter(GeoPoint(position.latitude, position.longitude))
}

internal fun MapView.currentPosition(): MapPosition =
  MapPosition(
    latitude = mapCenter.latitude,
    longitude = mapCenter.longitude,
    zoom = zoomLevelDouble,
    orientation = mapOrientation
  )

internal fun MapView.venueMarker(marker: MapMarker.SingleVenue, drawable: Drawable): Marker =
  Marker(this).apply {
    position = GeoPoint(marker.venue.lat, marker.venue.lon)
    icon = drawable
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    infoWindow = null
  }

internal fun MapView.clusterMarker(marker: MapMarker.VenuesCluster, drawable: Drawable): Marker =
  Marker(this).apply {
    position = GeoPoint(marker.lat, marker.lon)
    icon = drawable
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    infoWindow = null
    setOnMarkerClickListener { _, _ ->
      val boundingBox = marker.getBoundingBox()
      if (
        boundingBox.latNorth != boundingBox.latSouth || boundingBox.lonEast != boundingBox.lonWest
      ) {
        zoomToBoundingBox(boundingBox.increaseByScale(1.15f), true)
      } else {
        setExpectedCenter(boundingBox.centerWithDateLine)
      }
      true
    }
  }

private fun MapMarker.VenuesCluster.getBoundingBox(): BoundingBox =
  BoundingBox(maxLat, maxLon, minLat, minLon)

internal fun BoundingBox.toBounds(): MapBounds =
  MapBounds(minLat = latSouth, maxLat = latNorth, minLon = lonWest, maxLon = lonEast)
