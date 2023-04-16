package com.trm.opencoinmap.core.common.view

import android.view.View

class SheetController(private val bottomSheetView: View, private val rightSheetView: View) {}

enum class SheetState {
  STATE_DRAGGING,
  STATE_SETTLING,
  STATE_EXPANDED,
  STATE_COLLAPSED,
  STATE_HIDDEN,
  STATE_HALF_EXPANDED,
}
