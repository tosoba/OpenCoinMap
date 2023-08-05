plugins {
  id("opencoinmap.android.feature")
  id("kotlin-parcelize")
}

android { namespace = "com.trm.opencoinmap.feature.venues" }

dependencies {
  implementation(libs.google.material)
  implementation(libs.materialLetterIcon)
  implementation("com.facebook.shimmer:shimmer:0.5.0")

  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)

  implementation(libs.liveEvent)

  implementation(libs.paging.common)
  implementation(libs.paging.runtime)
  implementation(libs.paging.rx)

  implementation(libs.timber)
}
