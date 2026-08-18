plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cometchat.ai.sampleapp.compose"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cometchat.ai.sampleapp.compose"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "6.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // CometChat v6 Compose UIKit
    implementation(project(":chatuikit-compose"))
    implementation(project(":chatuikit-core"))

    // CometChat Chat SDK (no calls SDK — text-only AI chat)
    implementation(libs.chat.sdk.android)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation Compose
    implementation(libs.navigation.compose)

    // Kotlin Serialization for type-safe navigation
    implementation(libs.kotlinx.serialization.json)

    // Gson for JSON serialization
    implementation(libs.gson)

    // OkHttp for network calls
    implementation(libs.okhttp)

    // Coil for image loading
    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.ui.tooling)
}
