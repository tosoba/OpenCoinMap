plugins {
    id("opencoinmap.android.library")
    id("opencoinmap.android.hilt")
    id("kotlinx-serialization")
}

android {
    android { namespace = "com.trm.opencoinmap.core.data" }

    testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))

    implementation(libs.rx.java)
    implementation(libs.rx.kotlin)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.work.ktx)
}
