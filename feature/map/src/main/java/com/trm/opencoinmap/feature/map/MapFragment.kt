package com.trm.opencoinmap.feature.map

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.feature.map.databinding.FragmentMapBinding
import com.trm.opencoinmap.feature.map.util.setDefaultConfig

class MapFragment : Fragment(R.layout.fragment_map) {
  private val viewBinding by viewBinding(FragmentMapBinding::bind)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    viewBinding.mapView.setDefaultConfig()
  }

  private enum class SavedStateKey {
    MAP_POSITION
  }
}
