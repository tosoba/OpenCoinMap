package com.trm.opencoinmap

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.provideDelegate
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/** Configure base Kotlin with Android options */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
  commonExtension.apply {
    compileSdk = 36

    defaultConfig { minSdk = 23 }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
      isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures { viewBinding = true }

    val warningsAsErrors: String? by project
    extensions.configure<KotlinAndroidProjectExtension> {
      compilerOptions {
        allWarningsAsErrors.set(warningsAsErrors.toBoolean())
        freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn", "-opt-in=kotlin.Experimental")
        jvmTarget.set(JvmTarget.JVM_17)
      }
    }
  }

  val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

  dependencies { add("coreLibraryDesugaring", libs.findLibrary("android.desugarJdkLibs").get()) }
}
