import com.android.build.api.dsl.ApplicationExtension
import com.screensnap.utils.ProjectConfig
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("kotlin-kapt")
                apply("com.google.dagger.hilt.android")
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = ProjectConfig.compileSdk

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
            }

            tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
        }
    }

}

