plugins {
    id("com.screensnap.plugin.android-app")
}

android {
    namespace = "com.screensnap.app"
}

dependencies {
    // Core modules
    implementation(project(":core_ui"))
    implementation(project(":core_screen_recorder"))
    implementation(project(":core_notification"))
    implementation(project(":core_datastore"))
    // Domain modules
    implementation(project(":domain_navigation"))
    // Feature modules
    implementation(project(":feature_home"))
    implementation(project(":feature_settings"))
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}