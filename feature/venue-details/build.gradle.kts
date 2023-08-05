plugins {
  id("opencoinmap.android.feature")
  id("kotlin-parcelize")
}

android { namespace = "com.trm.opencoinmap.feature.venue.details" }

dependencies {
  implementation(libs.google.material)

  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)

  implementation(libs.liveEvent)

  implementation(libs.timber)
}
