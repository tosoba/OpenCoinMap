plugins {
  id("opencoinmap.android.feature")
  id("kotlin-parcelize")
}

android { namespace = "com.trm.opencoinmap.feature.about" }

dependencies {
  implementation(libs.google.material)

  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)

  implementation(libs.timber)
}
