package com.trm.opencoinmap.core.domain.model

sealed interface Message {
  object Hidden : Message

  data class Shown(val textResId: Int, val length: Length, val action: Action? = null) : Message {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as Shown

      if (textResId != other.textResId) return false
      if (length != other.length) return false

      return true
    }

    override fun hashCode(): Int {
      var result = textResId
      result = 31 * result + length.hashCode()
      return result
    }
  }

  enum class Length {
    SHORT,
    LONG,
    INDEFINITE
  }

  data class Action(val labelResId: Int, private val action: () -> Unit) : () -> Unit by action
}
