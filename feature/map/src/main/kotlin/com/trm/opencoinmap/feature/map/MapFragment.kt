package com.trm.opencoinmap.feature.map

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.calculateLatLonDivisors
import com.trm.opencoinmap.core.domain.model.LatLng
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.feature.map.databinding.FragmentMapBinding
import com.trm.opencoinmap.feature.map.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map) {
  private val binding by viewBinding(FragmentMapBinding::bind)
  private val viewModel by viewModels<MapViewModel>()

  @Inject internal lateinit var clusterMarkerIconBuilder: ClusterMarkerIconBuilder
  @Inject internal lateinit var markerClusterer: RadiusMarkerSizeClusterer

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.mapView.init()
    viewModel.observe()
  }

  override fun onDestroyView() {
    binding.mapView.onDetach()
    super.onDestroyView()
  }

  override fun onResume() {
    super.onResume()
    binding.mapView.onResume()
  }

  override fun onPause() {
    binding.mapView.onPause()
    super.onPause()
  }

  private fun MapViewModel.observe() {
    isLoading.observe(viewLifecycleOwner, binding.loadingIndicator::isVisible::set)
  }

  private fun MapView.init() {
    setDefaultConfig()

    val (latDivisor, lonDivisor) = resources.configuration.calculateLatLonDivisors()
    fun MapViewModel.onMapUpdated() {
      onMapUpdated(
        boundingBox = boundingBox,
        position = position,
        latDivisor = latDivisor,
        lonDivisor = lonDivisor
      )
    }

    addMapListener(
      object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean = onMapInteraction()
        override fun onZoom(event: ZoomEvent?): Boolean = onMapInteraction()

        fun onMapInteraction(): Boolean {
          viewModel.onMapUpdated()
          return false
        }
      }
    )

    addOnFirstLayoutListener { _, _, _, _, _ -> viewModel.onMapUpdated() }

    var firstPosition = true
    viewModel.mapPositionUpdate.observe(viewLifecycleOwner) { (mapPosition, shouldUpdate) ->
      if (shouldUpdate || firstPosition) position = mapPosition
      firstPosition = false
    }

    var userLocation: LatLng? = null
    val userLocationMarkerDrawable by
      lazy(LazyThreadSafetyMode.NONE) {
        requireNotNull(ContextCompat.getDrawable(requireContext(), R.drawable.user_location_marker))
      }

    fun MapView.userLocationMarker(location: LatLng): Marker =
      userLocationMarker(location, userLocationMarkerDrawable)

    viewModel.userLocation.observe(viewLifecycleOwner) { location ->
      userLocation = location
      overlays.add(
        userLocationMarker(location).also { marker ->
          overlays.add(marker)
          controller.animateTo(
            marker.position,
            maxOf(MapDefaults.VENUE_LOCATION_ZOOM, zoomLevelDouble),
            MapDefaults.ANIMATION_DURATION_MS
          )
        }
      )
    }

    val venueDrawable by
      lazy(LazyThreadSafetyMode.NONE) {
        requireNotNull(ContextCompat.getDrawable(requireContext(), R.drawable.venue_marker))
      }
    viewModel.markersInBounds.observe(viewLifecycleOwner) { markers ->
      markerClusterer.items.clear()
      markerClusterer.invalidate()

      overlays.clear()
      markers.map { marker ->
        when (marker) {
          is MapMarker.SingleVenue -> {
            markerClusterer.add(
              venueMarker(
                marker = marker,
                drawable = venueDrawable,
                onClick = viewModel::onVenueMarkerClick
              )
            )
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
      userLocation?.let { overlays.add(userLocationMarker(it)) }
      addCopyrightOverlay()

      invalidate()
    }
  }
}
