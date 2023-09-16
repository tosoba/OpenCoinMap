package com.trm.opencoinmap.core.domain.model

sealed interface Message {
  object Hidden : Message
  data class Shown(val textResId: Int, val length: Length, val action: Action? = null) : Message

  enum class Length {
    SHORT,
    LONG,
    INDEFINITE
  }

  data class Action(val labelResId: Int, private val action: () -> Unit) : () -> Unit by action
}
