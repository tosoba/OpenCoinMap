package com.trm.opencoinmap.feature.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.calculateLatLonDivisors
import com.trm.opencoinmap.core.domain.model.MapMarker
import com.trm.opencoinmap.feature.map.databinding.FragmentMapBinding
import com.trm.opencoinmap.feature.map.model.BoundingBoxArgs
import com.trm.opencoinmap.feature.map.util.currentPosition
import com.trm.opencoinmap.feature.map.util.restorePosition
import com.trm.opencoinmap.feature.map.util.setDefaultConfig
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@AndroidEntryPoint
class MapFragment : Fragment(R.layout.fragment_map) {
  private val viewBinding by viewBinding(FragmentMapBinding::bind)
  private val viewModel by viewModels<MapViewModel>()

  private val clusterSizeTextPaint: Paint by
    lazy(LazyThreadSafetyMode.NONE) { requireContext().clusterSizeTextPaint() }

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
      onBoundingBox(
        BoundingBoxArgs(boundingBox = boundingBox, latDivisor = latDivisor, lonDivisor = lonDivisor)
      )
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

    val venueDrawable =
      requireNotNull(ContextCompat.getDrawable(requireContext(), R.drawable.venue_marker))
    val clusterDrawable =
      requireNotNull(ContextCompat.getDrawable(requireContext(), R.drawable.cluster_marker))
    val clusterBitmap = clusterDrawable.toBitmap()
    viewModel.markersInBounds.observe(viewLifecycleOwner) { markers ->
      val clusterer = RadiusMarkerClusterer(requireContext()).apply { setIcon(clusterBitmap) }
      overlays.clear()
      markers.map { marker ->
        when (marker) {
          is MapMarker.SingleVenue -> {
            clusterer.add(venueMarker(marker = marker, drawable = venueDrawable))
          }
          is MapMarker.VenuesCluster -> {
            overlays.add(clusterMarker(marker = marker, bitmap = clusterBitmap))
          }
        }
      }
      overlays.add(clusterer)
      invalidate()
    }
  }

  private fun MapView.venueMarker(marker: MapMarker.SingleVenue, drawable: Drawable): Marker =
    Marker(this).apply {
      position = GeoPoint(marker.venue.lat, marker.venue.lon)
      icon = drawable
      setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
      infoWindow = null
    }

  private fun MapView.clusterMarker(marker: MapMarker.VenuesCluster, bitmap: Bitmap): Marker =
    Marker(this).apply {
      position = GeoPoint(marker.lat, marker.lon)
      icon = buildClusterIcon(size = marker.size, bitmap = bitmap)
      setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
      infoWindow = null
    }

  private fun buildClusterIcon(size: Int, bitmap: Bitmap): BitmapDrawable {
    val densityDpi = requireContext().resources.displayMetrics.densityDpi
    val icon =
      Bitmap.createBitmap(
        bitmap.getScaledWidth(densityDpi),
        bitmap.getScaledHeight(densityDpi),
        bitmap.config
      )
    val iconCanvas = Canvas(icon)
    iconCanvas.drawBitmap(bitmap, 0f, 0f, null)
    val textHeight = (clusterSizeTextPaint.descent() + clusterSizeTextPaint.ascent()).toInt()
    iconCanvas.drawText(
      size.toString(),
      Marker.ANCHOR_CENTER * icon.width,
      Marker.ANCHOR_CENTER * icon.height - textHeight / 2,
      clusterSizeTextPaint
    )
    return BitmapDrawable(requireContext().resources, icon)
  }
}

fun Context.clusterSizeTextPaint(): Paint =
  Paint().apply {
    color = Color.WHITE
    textSize = 15 * resources.displayMetrics.density
    isFakeBoldText = true
    textAlign = Paint.Align.CENTER
    isAntiAlias = true
  }
