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
    implementation(project(":core_datastore"))
    // Feature modules
    implementation(project(":feature_home"))
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}