plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.cometchat.sampleapp.kotlin"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cometchat.sampleapp.kotlin"
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
    }
    
    // Enable JUnit 5 for Kotest property-based testing
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    // CometChat UIKit SDK
//    implementation(libs.chatuikit.kotlin.android)
    implementation(project(":chatuikit-kotlin"))
    implementation(project(":chatuikit-core"))

    // CometChat Chat SDK
    implementation(libs.chat.sdk.android)
    
    // CometChat Calls SDK for Calls UI features (call buttons, call logs)
    implementation(libs.calls.sdk.android)
    
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Lifecycle components
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    // Navigation components
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    
    // Gson for JSON serialization
    implementation(libs.gson)
    
    // OkHttp for network calls
    implementation(libs.okhttp)
    
    // Glide for image loading
    implementation(libs.glide)
    
    // Unit testing
    testImplementation(libs.junit)
    
    // Mockito for mocking in unit tests
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    
    // Robolectric for Android framework classes in unit tests
    testImplementation(libs.robolectric)
    
    // Kotest for property-based testing
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    
    // JUnit Vintage engine to run JUnit 4 tests alongside JUnit 5
    testRuntimeOnly(libs.junit.vintage.engine)
    
    // Android instrumentation tests (E2E)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.espresso.contrib)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.uiautomator)
}
