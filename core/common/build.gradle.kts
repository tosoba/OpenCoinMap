plugins {
    id("opencoinmap.android.library")
    id("opencoinmap.android.hilt")
}

android { namespace = "com.trm.opencoinmap.core.common" }

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.osmdroid)
}
