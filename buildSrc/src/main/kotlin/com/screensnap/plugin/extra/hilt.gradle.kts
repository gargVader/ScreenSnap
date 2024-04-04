package com.screensnap.plugin.extra

import com.screensnap.utils.implementation
import com.screensnap.utils.kapt
import org.gradle.kotlin.dsl.dependencies

apply(plugin = "com.google.dagger.hilt.android")

dependencies {
    // Hilt
    implementation("com.google.dagger:hilt-android:2.44.2")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}