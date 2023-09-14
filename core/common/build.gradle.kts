plugins {
  id("opencoinmap.android.library")
  id("opencoinmap.android.hilt")
}

android { namespace = "com.trm.opencoinmap.core.common" }

dependencies {
  implementation(project(":core:domain"))

  implementation(libs.google.material)

  implementation(libs.osmdroid)
  implementation(libs.play.services.location)

  api(libs.rx.android)
  api(libs.rx.java)
  api(libs.rx.kotlin)
  api(libs.rx.relay)
}
