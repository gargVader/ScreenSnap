pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "ScreenSnap"
include(":app")
include(":core_ui")
include(":core_screen_recorder")
include(":core_datastore")
include(":core_notification")
include(":core_camera")
include(":domain_navigation")
include(":feature_home")
include(":feature_settings")
