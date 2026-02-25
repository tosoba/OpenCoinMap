plugins {
  id("opencoinmap.android.application")
  id("opencoinmap.android.hilt")
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.trm.opencoinmap"

  defaultConfig {
    applicationId = "com.trm.opencoinmap"
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    vectorDrawables { useSupportLibrary = true }
  }

  buildTypes {
    named("release") {
      isMinifyEnabled = false
      setProguardFiles(
        listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      )
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures { compose = true }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:data"))
  implementation(project(":core:database"))
  implementation(project(":core:datastore"))
  implementation(project(":core:domain"))
  implementation(project(":core:network"))
  implementation(project(":core:ui"))

  implementation(project(":feature:about"))
  implementation(project(":feature:categories"))
  implementation(project(":feature:map"))
  implementation(project(":feature:venue-details"))
  implementation(project(":feature:venues"))

  implementation(project(":sync"))

  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.google.material)
  implementation(libs.viewBinding.propertyDelegate)

  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundation.layout)
  implementation(libs.androidx.compose.material.iconsExtended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.runtime.livedata)
  implementation(libs.androidx.compose.ui)

  implementation(libs.paging.runtime)
  implementation(libs.paging.rx)

  implementation(libs.osmdroid)
  implementation(libs.timber)

  testImplementation(libs.junit4)
}
