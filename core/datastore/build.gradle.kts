plugins {
  id("opencoinmap.android.library")
  id("opencoinmap.android.hilt")
}

android { namespace = "com.trm.opencoinmap.core.datastore" }

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:domain"))

  implementation(libs.androidx.dataStore.core)
  implementation(libs.androidx.dataStore.preferences)

  implementation(libs.kotlinx.coroutines.rx3)
}
