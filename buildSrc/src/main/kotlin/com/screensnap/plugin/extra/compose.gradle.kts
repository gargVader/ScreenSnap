package com.screensnap.plugin.extra

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.screensnap.utils.androidTestImplementation
import com.screensnap.utils.debugImplementation
import com.screensnap.utils.implementation

extensions.findByType<LibraryExtension>()?.apply {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

extensions.findByType<ApplicationExtension>()?.apply {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Navigation in compose
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Icons extended
    implementation("androidx.compose.material:material-icons-extended:1.6.2")

    // Accompanist
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.28.0")
}