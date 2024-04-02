plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
    implementation("com.android.tools.build:gradle:8.3.1")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.44")
}

gradlePlugin{
    plugins{
        register("androidApplication"){
            id = "com.screensnap.plugins.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }

        register("androidLibraryCompose"){
            id = "com.screensnap.plugins.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
    }
}
