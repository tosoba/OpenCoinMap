plugins { id("kotlin") }

dependencies {
  implementation(libs.rx.java)
  implementation(libs.rx.kotlin)
  implementation(libs.rx.relay)

  implementation(libs.dagger.dagger)
}
