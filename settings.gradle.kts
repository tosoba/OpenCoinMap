pluginManagement {
  includeBuild("build-logic")
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { url = java.net.URI("https://jitpack.io") }
  }
}

rootProject.name = "OpenCoinMap"

include(":app")

include(":core:common")

include(":core:datastore")

include(":core:domain")

include(":core:data")

include(":core:database")

include(":core:network")

include(":feature:about")

include(":feature:categories")

include(":feature:map")

include(":feature:venue-details")

include(":feature:venues")

include(":sync")
