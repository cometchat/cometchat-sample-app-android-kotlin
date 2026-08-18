plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.cometchat.ai.sampleapp.kotlin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cometchat.ai.sampleapp.kotlin"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    // CometChat v6 UIKit
    implementation(project(":chatuikit-kotlin"))
    implementation(project(":chatuikit-core"))

    // CometChat Chat SDK
    implementation(libs.chat.sdk.android)

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // DrawerLayout for the chat history drawer
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // Gson for JSON serialization
    implementation(libs.gson)

    // OkHttp for network calls
    implementation(libs.okhttp)

    // Glide for image loading
    implementation(libs.glide)
}
