package com.trm.opencoinmap.core.common.ext

import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.addOnScrollIdleListener(action: () -> Unit) {
  addOnScrollListener(
    object : RecyclerView.OnScrollListener() {
      override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) action()
      }
    }
  )
}
