package com.trm.opencoinmap.feature.map

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.calculateLatLonDivisors
import com.trm.opencoinmap.core.domain.model.*
import com.trm.opencoinmap.feature.map.databinding.FragmentMapBinding
import com.trm.opencoinmap.feature.map.util.currentPosition
import com.trm.opencoinmap.feature.map.util.restorePosition
import com.trm.opencoinmap.feature.map.util.setDefaultConfig
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
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
    observeLoading()
  }

  private fun observeLoading() {
    viewModel.isLoading.observe(viewLifecycleOwner, viewBinding.loadingIndicator::isVisible::set)
  }

  private fun MapView.init() {
    setDefaultConfig()
    restorePosition(viewModel.mapPosition)

    val (latDivisor, lonDivisor) = resources.configuration.calculateLatLonDivisors()
    fun MapViewModel.onBoundingBox(boundingBox: BoundingBox) {
      onBoundingBox(boundingBox = boundingBox, latDivisor = latDivisor, lonDivisor = lonDivisor)
    }

    addMapListener(
      object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean = onMapInteraction()
        override fun onZoom(event: ZoomEvent?): Boolean = onMapInteraction()

        fun onMapInteraction(): Boolean {
          viewModel.mapPosition = currentPosition()
          viewModel.onBoundingBox(boundingBox)
          return false
        }
      }
    )

    addOnFirstLayoutListener { _, _, _, _, _ -> viewModel.onBoundingBox(boundingBox) }

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

  private fun MapView.venueMarker(marker: MapMarker.SingleVenue, drawable: Drawable?): Overlay =
    Marker(this).apply {
      position = GeoPoint(marker.venue.lat, marker.venue.lon)
      image = drawable
      setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }

  private fun MapView.clusterMarker(marker: MapMarker.VenuesCluster, drawable: Drawable?): Overlay =
    Marker(this).apply {
      position = GeoPoint(marker.lat, marker.lon)
      image = drawable
      setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
    }
}
