package com.screensnap.plugin

import com.android.build.api.dsl.ApplicationExtension
import com.screensnap.utils.ProjectConfig
import com.screensnap.utils.androidTestImplementation
import com.screensnap.utils.implementation
import com.screensnap.utils.testImplementation
import com.screensnap.utils.testImplementation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(plugin = "com.android.application")
apply(plugin = "org.jetbrains.kotlin.android")
apply(plugin = "kotlin-kapt")
apply(plugin = "com.screensnap.plugin.extra.compose")
apply(plugin = "com.screensnap.plugin.extra.hilt")
apply(plugin = "com.screensnap.plugin.android-base")

configure<ApplicationExtension> {

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

