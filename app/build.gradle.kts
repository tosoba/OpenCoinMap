plugins {
  id("opencoinmap.android.application")
  id("opencoinmap.android.hilt")
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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions { jvmTarget = "1.8" }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(project(":core:common"))
  implementation(project(":core:data"))
  implementation(project(":core:database"))
  implementation(project(":core:domain"))
  implementation(project(":core:network"))
  implementation(project(":core:ui"))

  implementation(project(":feature:map"))
  implementation(project(":feature:venues"))

  implementation(project(":sync"))

  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.google.material)
  implementation(libs.viewBinding.propertyDelegate)
  implementation(libs.rightSheetBehavior)

  implementation(libs.rx.android)
  implementation(libs.rx.java)
  implementation(libs.rx.kotlin)
  implementation(libs.rx.relay)
  implementation(libs.liveEvent)

  implementation(libs.paging.runtime)
  implementation(libs.paging.rx)

  implementation(libs.osmdroid)
  implementation(libs.timber)

  testImplementation(libs.junit4)
}
