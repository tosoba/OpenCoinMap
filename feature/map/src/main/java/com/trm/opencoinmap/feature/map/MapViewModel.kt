package com.trm.opencoinmap.feature.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.common.view.get
import com.trm.opencoinmap.core.domain.usecase.GetVenuesUseCase
import com.trm.opencoinmap.feature.map.model.MapPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MapViewModel
@Inject
constructor(
  savedStateHandle: SavedStateHandle,
  private val getVenuesUseCase: GetVenuesUseCase,
) : ViewModel() {
  var mapPosition by savedStateHandle.get(defaultValue = MapPosition())
}
