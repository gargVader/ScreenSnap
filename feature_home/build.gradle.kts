plugins {
    id("com.screensnap.plugin.feature")
}

android {
    namespace = "com.screensnap.feature.home"
}

dependencies {
    // Project
    implementation(project(":core_ui"))
    implementation(project(":core_datastore"))
    implementation(project(":core_screen_recorder"))
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}