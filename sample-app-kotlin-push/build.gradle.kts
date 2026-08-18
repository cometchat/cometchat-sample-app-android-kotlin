plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

configurations.all {
    exclude(group = "org.jetbrains", module = "annotations-java5")
}

android {
    namespace = "com.cometchat.sampleapp.kotlin.push"
    compileSdk = 36


    defaultConfig {
        applicationId = "com.cometchat.sampleapp.kotlin.push"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "6.0.1"

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
    implementation(project(":chatuikit-kotlin"))
    implementation(project(":chatuikit-core"))
    implementation(libs.chat.sdk.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.process)
    
    // Gson for JSON serialization
    implementation(libs.gson)
    
    // OkHttp for network calls
    implementation(libs.okhttp)
    
    // FlexboxLayout for color palette in showcase property controls
    implementation(libs.flexbox)
    
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
    testRuntimeOnly(libs.junit.vintage.engine.v582)

    implementation(libs.calls.sdk.android)

    // CometChat Push Notifications SDK
    implementation(libs.push.notifications.android)

    // Firebase Cloud Messaging
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
