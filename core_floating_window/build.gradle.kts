plugins {
    id("com.screensnap.plugin.core")
    id("com.screensnap.plugin.extra.compose")
}

android {
    namespace = "com.screensnap.core.floating_window"
}

dependencies {
    // Project

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}