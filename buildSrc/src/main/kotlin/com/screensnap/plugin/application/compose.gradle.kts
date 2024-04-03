package com.screensnap.plugin.application

import com.android.build.api.dsl.ApplicationExtension
import com.screensnap.plugin.configureAndroidCompose
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

apply(plugin = "com.android.application")

configure<ApplicationExtension> {
    configureAndroidCompose(this)
}