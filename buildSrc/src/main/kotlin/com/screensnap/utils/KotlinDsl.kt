package com.screensnap.utils

import org.gradle.api.artifacts.dsl.DependencyHandler


fun DependencyHandler.dependsAs(configuration: String, dependency: Any){
    if (dependency is String){
        add(configuration, dependency)
    } else {
        add(configuration, dependency)
    }
}

fun DependencyHandler.implementation(dependency: Any){
    dependsAs("implementation", dependency)
}

fun DependencyHandler.testImplementation(dependency: Any){
    dependsAs("testImplementation", dependency)
}

fun DependencyHandler.androidTestImplementation(dependency: Any){
    dependsAs("androidTestImplementation", dependency)
}

fun DependencyHandler.debugImplementation(dependency: Any){
    dependsAs("debugImplementation", dependency)
}

fun DependencyHandler.kapt(dependency: Any){
    dependsAs("kapt", dependency)
}