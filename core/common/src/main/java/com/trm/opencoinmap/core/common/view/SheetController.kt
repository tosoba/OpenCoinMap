package com.trm.opencoinmap.core.common.view

import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import com.google.android.material.bottomsheet.BottomSheetBehavior
import eu.okatrych.rightsheet.RightSheetBehavior

class SheetController(
  private val bottomSheetView: View,
  private val rightSheetView: View,
  private val collapsedAlpha: Float,
  private val initialState: SheetState = SheetState.STATE_COLLAPSED,
  private val onStateChanged: (SheetState) -> Unit = {}
) {
  private val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
  private val rightSheetBehavior = RightSheetBehavior.from(rightSheetView)

  var state = initialState
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

          state = SheetState.fromBottom(newState)
          rightSheetBehavior.state = state.rightSheetState
          onStateChanged(state)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
          updateSheetContainersAlpha(slideOffset)
        }
      }
    )

    rightSheetBehavior.addRightSheetCallback(
      object : RightSheetBehavior.RightSheetCallback() {
        override fun onStateChanged(rightSheet: View, newState: Int) {
          if (
            newState == RightSheetBehavior.STATE_SETTLING ||
              newState == RightSheetBehavior.STATE_DRAGGING
          ) {
            return
          }

          state = SheetState.fromRight(newState)
          bottomSheetBehavior.state = state.bottomSheetState
          onStateChanged(state)
        }

        override fun onSlide(rightSheet: View, slideOffset: Float) {
          updateSheetContainersAlpha(slideOffset)
        }
      }
    )

    setState(
      if (savedInstanceState != null) {
        SheetState.valueOf(requireNotNull(savedInstanceState.getString(key)))
      } else {
        initialState
      }
    )
    onStateChanged(state)
    updateSheetContainersAlpha(if (state == SheetState.STATE_EXPANDED) 1f else 0f)
  }

  @MainThread
  fun saveState(outState: Bundle, key: String = SHEET_STATE) {
    outState.putString(key, state.name)
  }

  @MainThread
  fun setState(state: SheetState) {
    bottomSheetBehavior.state = state.bottomSheetState
    rightSheetBehavior.state = state.rightSheetState
    this.state = state
  }

  private fun updateSheetContainersAlpha(slideOffset: Float) {
    val alpha = collapsedAlpha + slideOffset * (1f - collapsedAlpha)
    bottomSheetView.alpha = alpha
    rightSheetView.alpha = alpha
  }

  companion object {
    private const val SHEET_STATE = "SHEET_STATE"
  }
}

enum class SheetState {
  STATE_DRAGGING,
  STATE_SETTLING,
  STATE_EXPANDED,
  STATE_COLLAPSED,
  STATE_HIDDEN,
  STATE_HALF_EXPANDED;

  companion object {
    fun fromBottom(@BottomSheetBehavior.State state: Int): SheetState =
      when (state) {
        BottomSheetBehavior.STATE_COLLAPSED -> STATE_COLLAPSED
        BottomSheetBehavior.STATE_DRAGGING -> STATE_DRAGGING
        BottomSheetBehavior.STATE_EXPANDED -> STATE_EXPANDED
        BottomSheetBehavior.STATE_HALF_EXPANDED -> STATE_HALF_EXPANDED
        BottomSheetBehavior.STATE_HIDDEN -> STATE_HIDDEN
        BottomSheetBehavior.STATE_SETTLING -> STATE_SETTLING
        else -> throw IllegalArgumentException()
      }

    fun fromRight(@RightSheetBehavior.State state: Int): SheetState =
      when (state) {
        RightSheetBehavior.STATE_COLLAPSED -> STATE_COLLAPSED
        RightSheetBehavior.STATE_DRAGGING -> STATE_DRAGGING
        RightSheetBehavior.STATE_EXPANDED -> STATE_EXPANDED
        RightSheetBehavior.STATE_HALF_EXPANDED -> STATE_HALF_EXPANDED
        RightSheetBehavior.STATE_HIDDEN -> STATE_HIDDEN
        RightSheetBehavior.STATE_SETTLING -> STATE_SETTLING
        else -> throw IllegalArgumentException()
      }
  }

  @BottomSheetBehavior.State
  val bottomSheetState: Int
    get() =
      when (this) {
        STATE_DRAGGING -> BottomSheetBehavior.STATE_DRAGGING
        STATE_SETTLING -> BottomSheetBehavior.STATE_SETTLING
        STATE_EXPANDED -> BottomSheetBehavior.STATE_EXPANDED
        STATE_COLLAPSED -> BottomSheetBehavior.STATE_COLLAPSED
        STATE_HIDDEN -> BottomSheetBehavior.STATE_HIDDEN
        STATE_HALF_EXPANDED -> BottomSheetBehavior.STATE_HALF_EXPANDED
      }

  @RightSheetBehavior.State
  val rightSheetState: Int
    get() =
      when (this) {
        STATE_DRAGGING -> RightSheetBehavior.STATE_DRAGGING
        STATE_SETTLING -> RightSheetBehavior.STATE_SETTLING
        STATE_EXPANDED -> RightSheetBehavior.STATE_EXPANDED
        STATE_COLLAPSED -> RightSheetBehavior.STATE_COLLAPSED
        STATE_HIDDEN -> RightSheetBehavior.STATE_HIDDEN
        STATE_HALF_EXPANDED -> RightSheetBehavior.STATE_HALF_EXPANDED
      }
}
