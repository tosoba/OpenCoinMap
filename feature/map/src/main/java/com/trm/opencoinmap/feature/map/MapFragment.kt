package com.trm.opencoinmap.feature.map

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.feature.map.databinding.FragmentMapBinding
import com.trm.opencoinmap.feature.map.model.MapPosition
import com.trm.opencoinmap.feature.map.util.restorePosition
import com.trm.opencoinmap.feature.map.util.setDefaultConfig
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map) {
  private val viewBinding by viewBinding(FragmentMapBinding::bind)
  private val viewModel by viewModels<MapViewModel>()
  private val compositeDisposable = CompositeDisposable()

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
        val overlay = FolderOverlay()
        venues.forEach { venue ->
          overlay.add(
            Marker(this).apply {
              position = GeoPoint(venue.lat, venue.lon)
              image = markerDrawable
              setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
          )
        }

        overlays.add(overlay)
      }
      .addTo(compositeDisposable)
  }

  override fun onDestroyView() {
    compositeDisposable.clear()
    super.onDestroyView()
  }
}
