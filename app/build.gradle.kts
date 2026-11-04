plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "org.danigarcia.apps.simple_shopping_list"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "org.danigarcia.apps.simple_shopping_list"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Firma con la clave de debug: válida para distribución informal del APK
            // (no apta para Google Play). La clave es pública; no usar en producción.
            storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // Se desactiva el reempaquetado/optimización de R8: el packageScope
            // rompía la visibilidad de clases de la stdlib de Kotlin en runtime.
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Persistencia de ajustes (endpoint, auth, username)
    implementation(libs.androidx.datastore.preferences)

    // Red: Firebase Realtime Database via REST
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
}
