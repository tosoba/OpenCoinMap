package com.trm.opencoinmap.core.common.ext

inline fun <reified T> Any.safeAs(): T? = this as? T

inline fun <reified T> Any.requireAs(): T = this as T
