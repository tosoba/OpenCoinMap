plugins {
  id("opencoinmap.android.feature")
  id("kotlin-parcelize")
}

android { namespace = "com.trm.opencoinmap.feature.map" }

dependencies {
  implementation(libs.google.material)

  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)

  implementation(libs.rx.android)
  implementation(libs.rx.java)
  implementation(libs.rx.kotlin)
  implementation(libs.liveEvent)

  implementation(libs.osmdroid)
  implementation(libs.timber)
}
