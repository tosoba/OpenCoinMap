package com.trm.opencoinmap.core.domain.model

sealed interface Message {
  object Hidden : Message
  data class Shown(val text: String, val length: Length) : Message

  enum class Length {
    SHORT,
    LONG,
    INDEFINITE
  }
}
