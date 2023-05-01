package com.trm.opencoinmap.core.common.view

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import com.google.android.material.bottomsheet.BottomSheetBehavior

class SheetController(
  private val bottomSheetView: View,
  private val collapsedAlpha: Float,
  @BottomSheetBehavior.State private val initialState: Int = BottomSheetBehavior.STATE_COLLAPSED,
  private val onStateChanged: (Int) -> Unit = {},
  private val onSlide: (Float) -> Unit = {}
) {
  private val bottomSheetBehavior =
    BottomSheetBehavior.from(bottomSheetView).apply {
      setMaxWidth(ViewGroup.LayoutParams.MATCH_PARENT)
    }

  var state: Int = initialState
    private set

  init {
    require(collapsedAlpha > 0f && collapsedAlpha < 1f) {
      "Condition: 0f < collapsedAlpha < 1f is not met."
    }
  }

  @MainThread
  fun initFrom(savedInstanceState: Bundle?, key: String = SHEET_STATE) {
    bottomSheetBehavior.addBottomSheetCallback(
      object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
          if (
            newState == BottomSheetBehavior.STATE_SETTLING ||
              newState == BottomSheetBehavior.STATE_DRAGGING
          ) {
            return
          }

          state = newState
          onStateChanged(state)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
          updateSheetContainersAlpha(slideOffset)
          onSlide(slideOffset)
        }
      }
    )

    setState(
      if (savedInstanceState != null) requireNotNull(savedInstanceState.getInt(key))
      else initialState
    )
    onStateChanged(state)
    updateSheetContainersAlpha(if (state == BottomSheetBehavior.STATE_EXPANDED) 1f else 0f)
  }

  @MainThread
  fun saveState(outState: Bundle, key: String = SHEET_STATE) {
    outState.putInt(key, state)
  }

  @MainThread
  fun setState(@BottomSheetBehavior.State state: Int) {
    bottomSheetBehavior.state = state
    this.state = state
  }

  private fun updateSheetContainersAlpha(slideOffset: Float) {
    val alpha = collapsedAlpha + slideOffset * (1f - collapsedAlpha)
    bottomSheetView.alpha = alpha
  }

  companion object {
    private const val SHEET_STATE = "SHEET_STATE"
  }
}
