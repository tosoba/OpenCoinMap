plugins {
  id("opencoinmap.android.library")
  id("opencoinmap.android.hilt")
  id("kotlinx-serialization")
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.trm.opencoinmap.core.data"

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:database"))
  implementation(project(":core:domain"))
  implementation(project(":core:network"))

  implementation(libs.rx.java)
  implementation(libs.rx.kotlin)
  implementation(libs.rx.relay)

  implementation(libs.paging.common)
  implementation(libs.paging.runtime)
  implementation(libs.paging.rx)

  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  implementation(libs.room.rx)
  ksp(libs.room.compiler)

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.work.ktx)
}
