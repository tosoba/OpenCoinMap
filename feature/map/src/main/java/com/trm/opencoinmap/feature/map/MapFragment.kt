package com.trm.opencoinmap.feature.map

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.calculateLatLonDivisors
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.feature.map.databinding.FragmentMapBinding
import com.trm.opencoinmap.feature.map.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.osmdroid.api.IGeoPoint
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map) {
  private val viewBinding by viewBinding(FragmentMapBinding::bind)
  private val viewModel by viewModels<MapViewModel>()

  @Inject internal lateinit var clusterMarkerIconBuilder: ClusterMarkerIconBuilder
  @Inject internal lateinit var markerClusterer: RadiusMarkerSizeClusterer

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    viewBinding.mapView.init()
    viewModel.observeLoading()
  }

  private fun MapViewModel.observeLoading() {
    isLoading.observe(viewLifecycleOwner, viewBinding.loadingIndicator::isVisible::set)
  }

  private fun MapView.init() {
    setDefaultConfig()
    restorePosition(viewModel.mapPosition)

    val (latDivisor, lonDivisor) = resources.configuration.calculateLatLonDivisors()
    fun MapViewModel.onBoundingBoxChanged(boundingBox: BoundingBox, center: IGeoPoint) {
      onBoundingBoxChanged(
        boundingBox = boundingBox,
        center = center,
        latDivisor = latDivisor,
        lonDivisor = lonDivisor
      )
    }

    addMapListener(
      object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean = onMapInteraction()
        override fun onZoom(event: ZoomEvent?): Boolean = onMapInteraction()

        fun onMapInteraction(): Boolean {
          viewModel.mapPosition = currentPosition()
          viewModel.onBoundingBoxChanged(boundingBox, mapCenter)
          return false
        }
      }
    )

    addOnFirstLayoutListener { _, _, _, _, _ ->
      viewModel.onBoundingBoxChanged(boundingBox, mapCenter)
    }

    val venueDrawable =
      requireNotNull(ContextCompat.getDrawable(requireContext(), R.drawable.venue_marker))

    viewModel.markersInBounds.observe(viewLifecycleOwner) { markers ->
      markerClusterer.items.clear()
      markerClusterer.invalidate()

      overlays.clear()
      markers.map { marker ->
        when (marker) {
          is MapMarker.SingleVenue -> {
            markerClusterer.add(venueMarker(marker = marker, drawable = venueDrawable))
          }
          is MapMarker.VenuesCluster -> {
            overlays.add(
              clusterMarker(
                marker = marker,
                drawable = clusterMarkerIconBuilder.build(size = marker.size)
              )
            )
          }
        }
      }
      overlays.add(markerClusterer)
      addCopyrightOverlay()

      invalidate()
    }
  }
}
