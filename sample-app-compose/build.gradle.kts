plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.cometchat.sampleapp.compose"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cometchat.sampleapp.compose"
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
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    
    // Enable JUnit 5 for Kotest property-based testing
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    // CometChat UI Kit modules
//    implementation(libs.chatuikit.compose.android)

    implementation(project(":chatuikit-compose"))
    implementation(project(":chatuikit-core"))

    // CometChat SDKs
    implementation(libs.chat.sdk.android)
    implementation(libs.calls.sdk.android)

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
    
    // Unit testing
    testImplementation(libs.junit)
    
    // Kotest for property-based testing
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    
    // JUnit Vintage engine to run JUnit 4 tests alongside JUnit 5
    testRuntimeOnly(libs.junit.vintage.engine.v582)
    
    // Android instrumentation tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.androidx.rules.v161)
    
    // Debug dependencies for Compose tooling
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
