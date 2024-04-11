plugins {
    id("com.screensnap.plugin.core")
    id("com.screensnap.plugin.extra.compose")
}

android {
    namespace = "com.screensnap.core.ui"
}

dependencies {

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}