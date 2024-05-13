plugins {
    id("com.screensnap.plugin.core")
}

android {
    namespace = "com.screensnap.core.notification"
}

dependencies {
    // Project
    implementation(project(":core_ui"))
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}