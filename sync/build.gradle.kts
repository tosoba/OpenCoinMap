plugins {
  id("opencoinmap.android.library")
  id("opencoinmap.android.hilt")
}

android { namespace = "com.trm.opencoinmap.sync" }

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:domain"))

  implementation(libs.androidx.startup)

  implementation(libs.androidx.work.ktx)
  implementation(libs.androidx.work.rx)
  implementation(libs.rx.relay)

  implementation(libs.hilt.ext.work)
  ksp(libs.hilt.ext.compiler)

  implementation(libs.timber)

  androidTestImplementation(libs.androidx.work.testing)
}
