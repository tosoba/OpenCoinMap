package com.trm.opencoinmap.core.common.ext

inline fun <reified T> Any.takeIfInstance(): T? = this as? T
