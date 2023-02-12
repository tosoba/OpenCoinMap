package com.trm.opencoinmap.feature.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.common.view.get
import com.trm.opencoinmap.feature.map.model.MapPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MapViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : ViewModel() {
  var mapPosition by savedStateHandle.get(defaultValue = MapPosition())
}
