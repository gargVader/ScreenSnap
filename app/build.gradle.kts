plugins {
    id("com.screensnap.plugin.android-app")
}

android {
    namespace = "com.screensnap.app"
}

dependencies {
    // Project
    implementation(project(":core_screen_recorder"))
    implementation(project(":core_datastore"))
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}