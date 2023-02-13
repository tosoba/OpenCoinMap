package com.trm.opencoinmap.common.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <reified T> SavedStateHandle.get(
  key: String? = null,
  defaultValue: T
): ReadWriteProperty<Any, T> =
  object : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
      val stateKey = key ?: property.name
      return this@get[stateKey] ?: defaultValue
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
      val stateKey = key ?: property.name
      this@get[stateKey] = value
    }
  }

inline fun <reified T> SavedStateHandle.getLiveData(
  key: String? = null,
  initialValue: T,
): ReadOnlyProperty<Any, MutableLiveData<T>> = ReadOnlyProperty { _, property ->
  val stateKey = key ?: property.name
  if (initialValue == null) {
    this@getLiveData.getLiveData(stateKey)
  } else {
    this@getLiveData.getLiveData(stateKey, initialValue)
  }
}
