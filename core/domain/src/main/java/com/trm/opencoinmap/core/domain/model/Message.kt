package com.trm.opencoinmap.core.domain.model

sealed interface Message {
  object Hidden : Message
  data class Shown(val text: String, val length: Length, val action: Action?) : Message

  enum class Length {
    SHORT,
    LONG,
    INDEFINITE
  }

  data class Action(val label: String, private val action: () -> Unit) : () -> Unit by action
}
