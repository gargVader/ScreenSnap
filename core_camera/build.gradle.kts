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
    implementation(project(":core_floating_window"))
    implementation(project(":core_notification"))
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}