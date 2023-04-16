package com.trm.opencoinmap.core.common.view

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.trm.opencoinmap.core.common.ext.toSnackbarLength
import com.trm.opencoinmap.core.domain.model.Message

class SnackbarMessageObserver(
  private val view: View,
) : DefaultLifecycleObserver, Observer<Message> {
  private var snackbar: Snackbar? = null

  override fun onDestroy(owner: LifecycleOwner) {
    snackbar = null
  }

  override fun onChanged(message: Message?) {
    if (message == null) return

    snackbar =
      when (message) {
        is Message.Hidden -> {
          snackbar?.dismiss()
          null
        }
        is Message.Shown -> {
          Snackbar.make(view, message.textResId, message.length.toSnackbarLength())
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .addCallback(
              object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                  snackbar = null
                }
              }
            )
            .run {
              val action = message.action
              if (action == null) this else setAction(action.labelResId) { action() }
            }
            .apply(Snackbar::show)
        }
      }
  }
}
