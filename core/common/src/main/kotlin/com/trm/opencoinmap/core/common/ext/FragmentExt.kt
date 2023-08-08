package com.trm.opencoinmap.core.common.ext

import androidx.fragment.app.Fragment

inline fun <reified T> Fragment.findParentFragmentOfType(): T? {
  var parent = parentFragment
  do {
    if (parent is T) return parent
    parent = parent?.parentFragment
  } while (parent != null)
  return null
}
