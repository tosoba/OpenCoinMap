package com.trm.opencoinmap.feature.map

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.disposeOnDestroy
import com.trm.opencoinmap.feature.map.databinding.FragmentMapBinding
import com.trm.opencoinmap.feature.map.model.MapPosition
import com.trm.opencoinmap.feature.map.util.restorePosition
import com.trm.opencoinmap.feature.map.util.setDefaultConfig
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

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
    addMapListener(
      object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean = onMapInteraction()
        override fun onZoom(event: ZoomEvent?): Boolean = onMapInteraction()
        private fun onMapInteraction(): Boolean {
          viewModel.mapPosition =
            MapPosition(
              latitude = mapCenter.latitude,
              longitude = mapCenter.longitude,
              zoom = zoomLevelDouble,
              orientation = mapOrientation
            )
          viewModel.onBoundingBox(boundingBox)
          return false
        }
      }
    )

    val markerDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.marker)
    viewModel.venuesInBounds
      .subscribe { venues ->
        overlays.clear()
        venues.forEach { venue ->
          overlays.add(
            Marker(this).apply {
              position = GeoPoint(venue.lat, venue.lon)
              image = markerDrawable
              setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
          )
        }
        invalidate()
      }
      .disposeOnDestroy(viewLifecycleOwner.lifecycle)
  }
}
