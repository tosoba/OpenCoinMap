plugins {
  id("opencoinmap.android.feature")
  id("kotlin-parcelize")
}

android { namespace = "com.trm.opencoinmap.feature.venues" }

dependencies {
  implementation(libs.google.material)
  implementation(libs.materialLetterIcon)
  implementation(libs.facebook.shimmer)

  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)

  implementation(libs.liveEvent)

  implementation(libs.paging.common)
  implementation(libs.paging.runtime)
  implementation(libs.paging.rx)

  implementation(libs.timber)
}
