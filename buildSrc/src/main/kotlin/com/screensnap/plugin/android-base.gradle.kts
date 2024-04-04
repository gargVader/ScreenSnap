package com.screensnap.plugin

import com.android.build.gradle.BaseExtension
import com.screensnap.utils.ProjectConfig
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.configure


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
}