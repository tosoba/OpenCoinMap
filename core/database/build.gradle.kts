plugins {
  id("opencoinmap.android.library")
  id("opencoinmap.android.hilt")
  alias(libs.plugins.ksp)
}

android { namespace = "com.trm.opencoinmap.core.database" }

dependencies {
  implementation(libs.rx.java)
  implementation(libs.rx.kotlin)

  implementation(libs.room.runtime)
  implementation(libs.room.ktx)
  implementation(libs.room.rx)
  implementation(libs.room.paging)
  implementation(libs.room.paging.rx)
  ksp(libs.room.compiler)
}
