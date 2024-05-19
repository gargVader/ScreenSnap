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

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}