plugins {
    id("com.screensnap.plugin.android-app")
    id("com.screensnap.plugin.extra.compose")
    id("com.screensnap.plugin.extra.hilt")
}

android {
    namespace = "com.screensnap.app"

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.github.HBiSoft:HBRecorder:3.0.1")




    // Project
    implementation(project(":core_screen_recorder"))
    implementation(project(":core_datastore"))
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}