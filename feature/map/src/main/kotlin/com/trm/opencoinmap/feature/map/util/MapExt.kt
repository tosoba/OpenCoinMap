package com.trm.opencoinmap.feature.map.util

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.trm.opencoinmap.core.common.ext.toPx
import com.trm.opencoinmap.core.domain.model.MapBounds
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.core.domain.util.MapBoundsLimit
import com.trm.opencoinmap.feature.map.model.MapPosition
import kotlin.math.max
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay

internal fun MapView.setDefaultConfig(
  darkMode: Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
      Configuration.UI_MODE_NIGHT_YES
) {
  setTileSource(MapDefaults.tileSource)

  isTilesScaledToDpi = true
  setMultiTouchControls(true)
  zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

  isVerticalMapRepetitionEnabled = false
  setScrollableAreaLimitLatitude(
    MapView.getTileSystem().maxLatitude,
    MapView.getTileSystem().minLatitude,
    0
  )

  minZoomLevel =
    max(
      MapView.getTileSystem()
        .getLongitudeZoom(
          MapBoundsLimit.MAX_LON,
          MapBoundsLimit.MIN_LON,
          resources.configuration.screenWidthDp.toFloat().toPx(context).toInt()
        ),
      MapView.getTileSystem()
        .getLatitudeZoom(
          MapBoundsLimit.MAX_LAT,
          MapBoundsLimit.MIN_LAT,
          resources.configuration.screenHeightDp.toFloat().toPx(context).toInt()
        )
    )

  if (darkMode) overlayManager.tilesOverlay.setColorFilter(TilesOverlay.INVERT_COLORS)
  addCopyrightOverlay(darkMode)
}

internal fun MapView.addCopyrightOverlay(
  darkMode: Boolean =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
      Configuration.UI_MODE_NIGHT_YES
) {
  overlays.add(
    CopyrightOverlay(context).apply {
      setTextColor(if (darkMode) Color.WHITE else Color.BLACK)
      setCopyrightNotice(tileProvider.tileSource.copyrightNotice)
    }
  )
}

internal var MapView.position: MapPosition
  set(value) {
    controller.setZoom(value.zoom)
    mapOrientation = value.orientation
    setExpectedCenter(GeoPoint(value.latitude, value.longitude))
  }
  get() =
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
  BoundingBox(latNorth, lonWest, latSouth, lonEast)

internal fun BoundingBox.toBounds(): MapBounds =
  MapBounds(latSouth = latSouth, latNorth = latNorth, lonWest = lonWest, lonEast = lonEast)
