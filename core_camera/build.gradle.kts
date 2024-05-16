plugins {
    id("com.screensnap.plugin.core")
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