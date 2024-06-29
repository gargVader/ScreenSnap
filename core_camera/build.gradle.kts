plugins {
    id("com.screensnap.plugin.core")
    id("com.screensnap.plugin.extra.camera")
    id("com.screensnap.plugin.extra.compose")
}

android {
    namespace = "com.screensnap.core.camera"
}

dependencies {
    // Project
    implementation(project(":core_notification"))
//    implementation(project(":core_screen_recorder"))
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}