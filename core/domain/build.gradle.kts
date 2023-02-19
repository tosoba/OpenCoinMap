plugins {
    id("kotlin")
    id("kotlin-kapt")
}

dependencies {
    implementation(libs.rx.java)
    implementation(libs.rx.kotlin)

    implementation(libs.dagger.dagger)
    kapt(libs.dagger.compiler)
}
