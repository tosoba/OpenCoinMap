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
    vectorDrawables {
      useSupportLibrary = true
    }
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

  buildFeatures { viewBinding = true }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.google.material)

  testImplementation(libs.junit4)
}
