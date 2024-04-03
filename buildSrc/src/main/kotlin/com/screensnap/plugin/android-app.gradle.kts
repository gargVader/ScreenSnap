package com.screensnap.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.screensnap.utils.ProjectConfig
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(plugin = "com.android.application")
apply(plugin = "org.jetbrains.kotlin.android")
apply(plugin = "kotlin-kapt")
apply(plugin = "com.google.dagger.hilt.android")

configure<ApplicationExtension>{
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        targetSdk = ProjectConfig.targetSdk
        minSdk = ProjectConfig.minSdk
        versionCode = ProjectConfig.versionCode
        versionName = ProjectConfig.versionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

