package com.screensnap.plugin

import com.android.build.gradle.BaseExtension
import com.screensnap.utils.ProjectConfig
import com.screensnap.utils.androidTestImplementation
import com.screensnap.utils.implementation
import com.screensnap.utils.testImplementation
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.configure

apply(plugin = "kotlin-kapt")
apply(plugin = "org.jetbrains.kotlin.android")
apply(plugin = "com.screensnap.plugin.extra.hilt")

configure<BaseExtension> {
    compileSdkVersion(ProjectConfig.compileSdk)

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

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    dependencies {
        implementation("androidx.core:core-ktx:1.9.0")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    }
}