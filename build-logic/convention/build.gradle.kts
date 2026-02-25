plugins { `kotlin-dsl` }

group = "com.trm.opencoinmap"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
  implementation(libs.android.gradlePlugin)
  implementation(libs.kotlin.gradlePlugin)
  implementation(libs.ksp.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("androidApplication") {
      id = "opencoinmap.android.application"
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidLibrary") {
      id = "opencoinmap.android.library"
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidFeature") {
      id = "opencoinmap.android.feature"
      implementationClass = "AndroidFeatureConventionPlugin"
    }
    register("androidTest") {
      id = "opencoinmap.android.test"
      implementationClass = "AndroidTestConventionPlugin"
    }
    register("androidHilt") {
      id = "opencoinmap.android.hilt"
      implementationClass = "AndroidHiltConventionPlugin"
    }
  }
}
