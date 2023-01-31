plugins {
    id("opencoinmap.android.library")
}

android {
    android { namespace = "com.trm.opencoinmap.core.ui" }

    defaultConfig { testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
}

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.androidx.browser)
    implementation(libs.androidx.core.ktx)
    implementation(libs.coil.kt)

    api(libs.androidx.metrics)
    api(libs.androidx.tracing.ktx)

    implementation(libs.osmdroid)
}
