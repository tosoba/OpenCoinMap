import com.android.build.gradle.LibraryExtension
import com.trm.opencoinmap.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.kotlin

class AndroidFeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply {
        apply("opencoinmap.android.library")
        apply("opencoinmap.android.hilt")
      }

      //      extensions.configure<LibraryExtension> {
      //        defaultConfig {
      //          testInstrumentationRunner =
      // "com.trm.opencoinmap.core.testing.OpenCoinMapTestRunner"
      //        }
      //      }

      extensions.configure<LibraryExtension> { configureKotlinAndroid(this) }

      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      dependencies {
        add("implementation", project(":core:ui"))
        add("implementation", project(":core:data"))
        add("implementation", project(":core:common"))
        add("implementation", project(":core:domain"))

        add("implementation", libs.findLibrary("viewBinding.propertyDelegate").get())
        add("implementation", libs.findLibrary("coil.kt").get())

        add("testImplementation", kotlin("test"))
        add("androidTestImplementation", kotlin("test"))
      }
    }
  }
}
