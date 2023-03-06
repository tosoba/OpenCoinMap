package com.trm.opencoinmap.core.common.ext

import com.google.android.material.snackbar.BaseTransientBottomBar
import com.trm.opencoinmap.core.domain.model.Message

fun Message.Length.toSnackbarLength(): Int =
  when (this) {
    Message.Length.SHORT -> BaseTransientBottomBar.LENGTH_SHORT
    Message.Length.LONG -> BaseTransientBottomBar.LENGTH_LONG
    Message.Length.INDEFINITE -> BaseTransientBottomBar.LENGTH_INDEFINITE
  }
