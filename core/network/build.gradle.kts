plugins {
    id("opencoinmap.android.library")
    id("opencoinmap.android.hilt")
    id("kotlinx-serialization")
}

android {
    android { namespace = "com.trm.opencoinmap.core.network" }

    buildFeatures { buildConfig = true }
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)

    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    implementation(libs.retrofit.rxjava.adapter)

    implementation(libs.rx.android)
    implementation(libs.rx.java)
    implementation(libs.rx.kotlin)

    api(libs.junit4)

    testImplementation(libs.dagger.dagger)
    kaptTest(libs.dagger.compiler)
}
