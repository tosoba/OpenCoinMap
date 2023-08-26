package com.trm.opencoinmap.feature.venue.details

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.webkit.WebView

class VenueDetailsWebView(context: Context, attrs: AttributeSet?) : WebView(context, attrs) {
  constructor(context: Context) : this(context, null)

  override fun onTouchEvent(event: MotionEvent?): Boolean {
    when (event?.action) {
      MotionEvent.ACTION_UP -> performClick()
    }
    return true
  }

  override fun performClick(): Boolean {
    super.performClick()
    return true
  }
}
