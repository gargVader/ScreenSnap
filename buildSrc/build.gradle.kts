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
            id = "com.screensnap.plugin.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }

        register("androidApplicationCompose"){
            id = "com.screensnap.plugin.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }

        register("androidLibraryCompose"){
            id = "com.screensnap.plugin.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }

    }
}
