plugins {
    id("opencoinmap.android.library")
    id("opencoinmap.android.hilt")
    alias(libs.plugins.ksp)
}

android { namespace = "com.trm.opencoinmap.core.database" }

dependencies {
    implementation(libs.kotlinx.datetime)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
}
