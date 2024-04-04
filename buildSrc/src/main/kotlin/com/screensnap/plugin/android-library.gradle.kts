package com.screensnap.plugin

import com.android.build.api.dsl.LibraryExtension

apply(plugin = "com.android.library")
apply(plugin = "com.screensnap.plugin.android-base")

configure<LibraryExtension> {
    defaultConfig {
        consumerProguardFiles("proguard-rules.pro")
    }
}