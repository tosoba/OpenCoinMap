plugins {
    id("opencoinmap.android.library")
    id("opencoinmap.android.hilt")
}

android { namespace = "com.trm.opencoinmap.core.common" }

dependencies {
    implementation(project(":core:domain"))

    implementation(libs.osmdroid)

    implementation(libs.rx.android)
    implementation(libs.rx.java)
    implementation(libs.rx.kotlin)
}
