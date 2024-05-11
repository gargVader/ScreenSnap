plugins {
    id("com.screensnap.plugin.core")
}

android {
    namespace = "com.screensnap.core.screen_recorder"
}

dependencies {
    // Project
    implementation(project(":core_datastore"))
    implementation("androidx.media3:media3-session:1.3.1")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}