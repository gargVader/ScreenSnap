plugins {
    id("com.screensnap.plugin.core")
}

android {
    namespace = "com.screensnap.core.screen_recorder"
}

dependencies {
    // Project
    implementation(project(":core_datastore"))
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}