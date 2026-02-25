plugins {
  id("opencoinmap.android.library")
  id("opencoinmap.android.hilt")
  id("kotlinx-serialization")
}

android {
  namespace = "com.trm.opencoinmap.core.network"

  buildFeatures { buildConfig = true }
}

dependencies {
  implementation(project(":core:common"))

  implementation(libs.kotlinx.serialization.json)

  implementation(libs.okhttp.logging)
  implementation(libs.retrofit.core)
  implementation(libs.retrofit.kotlin.serialization)
  implementation(libs.retrofit.rxjava.adapter)

  api(libs.junit4)
}
