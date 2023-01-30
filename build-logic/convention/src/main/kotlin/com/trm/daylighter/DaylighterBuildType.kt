package com.trm.opencoinmap

/** This is shared between :app and :benchmarks module to provide configurations type safety. */
@Suppress("unused")
enum class OpenCoinMapBuildType(val applicationIdSuffix: String? = null) {
  DEBUG(".debug"),
  RELEASE,
  BENCHMARK(".benchmark")
}
