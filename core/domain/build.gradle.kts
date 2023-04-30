plugins { id("kotlin") }

dependencies {
  implementation(libs.dagger.dagger)

  implementation(libs.rx.java)
  implementation(libs.rx.kotlin)
  implementation(libs.rx.relay)

  implementation(libs.paging.common)
}
