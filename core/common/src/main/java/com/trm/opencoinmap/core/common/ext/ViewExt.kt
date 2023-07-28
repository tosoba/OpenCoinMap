package com.trm.opencoinmap.core.common.ext

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.trm.opencoinmap.core.domain.model.Message

fun Message.Length.toSnackbarLength(): Int =
  when (this) {
    Message.Length.SHORT -> BaseTransientBottomBar.LENGTH_SHORT
    Message.Length.LONG -> BaseTransientBottomBar.LENGTH_LONG
    Message.Length.INDEFINITE -> BaseTransientBottomBar.LENGTH_INDEFINITE
  }

fun View.hideAnimated() {
  if (isGone) return
  startAnimation(
    AnimationUtils.loadAnimation(context, android.R.anim.fade_out).apply {
      setAnimationListener(
        object : Animation.AnimationListener {
          override fun onAnimationStart(animation: Animation) = Unit
          override fun onAnimationEnd(animation: Animation) {
            isGone = true
          }

          override fun onAnimationRepeat(animation: Animation) = Unit
        }
      )
    }
  )
}

fun View.showAnimated() {
  if (isVisible) return
  startAnimation(
    AnimationUtils.loadAnimation(context, android.R.anim.fade_in).apply {
      setAnimationListener(
        object : Animation.AnimationListener {
          override fun onAnimationStart(animation: Animation) {
            isVisible = true
          }

          override fun onAnimationEnd(animation: Animation) = Unit
          override fun onAnimationRepeat(animation: Animation) = Unit
        }
      )
    }
  )
}
