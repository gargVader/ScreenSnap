package com.screensnap.plugin

import com.screensnap.utils.implementation

apply(plugin = "com.screensnap.plugin.android-library")
apply(plugin = "com.screensnap.plugin.extra.compose")

dependencies {
    implementation(project(":core_ui"))
    implementation(project(":domain_navigation"))
}