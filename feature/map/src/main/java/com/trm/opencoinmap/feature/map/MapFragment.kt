package com.trm.opencoinmap.feature.map

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.feature.map.databinding.FragmentMapBinding
import com.trm.opencoinmap.feature.map.model.MapPosition
import com.trm.opencoinmap.feature.map.util.restorePosition
import com.trm.opencoinmap.feature.map.util.setDefaultConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map) {
  private val viewBinding by viewBinding(FragmentMapBinding::bind)
  private val viewModel by viewModels<MapViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    viewBinding.mapView.init()
  }

  private fun MapView.init() {
    setDefaultConfig()
    restorePosition(viewModel.mapPosition)

    val widthDp = resources.configuration.screenWidthDp.toDouble()
    val heightDp = resources.configuration.screenHeightDp.toDouble()
    val multiplier = (max(widthDp, heightDp) / min(widthDp, heightDp)).roundToInt()
    val smallerDivisor = 3
    val largerDivisor = smallerDivisor * multiplier
    val latDivisor = if (heightDp > widthDp) largerDivisor else smallerDivisor
    val lonDivisor = if (widthDp > heightDp) largerDivisor else smallerDivisor

    addMapListener(
      object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean = onMapInteraction()
        override fun onZoom(event: ZoomEvent?): Boolean = onMapInteraction()

        fun onMapInteraction(): Boolean {
          viewModel.mapPosition =
            MapPosition(
              latitude = mapCenter.latitude,
              longitude = mapCenter.longitude,
              zoom = zoomLevelDouble,
              orientation = mapOrientation
            )
          viewModel.onBoundingBox(
            boundingBox = boundingBox,
            latDivisor = latDivisor,
            lonDivisor = lonDivisor
          )
          return false
        }
      }
    )

    addOnFirstLayoutListener { _, _, _, _, _ ->
      viewModel.onBoundingBox(
        boundingBox = boundingBox,
        latDivisor = latDivisor,
        lonDivisor = lonDivisor
      )
    }

    val markerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.marker)
    viewModel.markersInBounds.observe(viewLifecycleOwner) { markers ->
      overlays.clear()
      overlays.addAll(
        markers.map { marker ->
          when (marker) {
            is MapMarker.SingleVenue -> venueMarker(marker, markerDrawable)
            is MapMarker.VenuesCluster -> clusterMarker(marker, markerDrawable)
          }
        }
      )
      invalidate()
    }
  }

  private fun MapView.venueMarker(
    marker: MapMarker.SingleVenue,
    markerDrawable: Drawable?
  ): Overlay =
    Marker(this).apply {
      position = GeoPoint(marker.venue.lat, marker.venue.lon)
      image = markerDrawable
      setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }

  private fun MapView.clusterMarker(
    marker: MapMarker.VenuesCluster,
    markerDrawable: Drawable?
  ): Overlay =
    Marker(this).apply {
      position = GeoPoint(marker.lat, marker.lon)
      image = markerDrawable
      setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }
}
