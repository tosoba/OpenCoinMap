package com.trm.opencoinmap.feature.venue.details

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView

class VenueDetailsWebView(context: Context, attrs: AttributeSet?) : WebView(context, attrs) {
  constructor(context: Context) : this(context, null)

  var interactionDisabled = false

  override fun onTouchEvent(event: MotionEvent?): Boolean {
    if (!interactionDisabled) return super.onTouchEvent(event)

    when (event?.action) {
      MotionEvent.ACTION_UP -> performClick()
    }
    return true
  }

  override fun performClick(): Boolean {
    if (!interactionDisabled) return super.performClick()

    super.performClick()
    return true
  }
}
