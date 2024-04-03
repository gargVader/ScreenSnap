package com.screensnap.plugin.library

import com.android.build.api.dsl.LibraryExtension
import com.screensnap.plugin.configureAndroidCompose
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

apply(plugin = "com.android.library")

configure<LibraryExtension> {
    configureAndroidCompose(this)
}