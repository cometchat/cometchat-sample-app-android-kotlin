
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

// Get version information from properties or environment

android {
    namespace = "com.cometchat.uikit.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.useJUnitPlatform()
            it.testLogging {
                events("passed", "failed", "skipped")
                showStandardStreams = true
            }
        }
    }
}


dependencies {
    // CometChat SDKs – exposed to consumers
    api(libs.chat.sdk.android)
    compileOnly(libs.calls.sdk.android)
    implementation(libs.cards.android)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    // Real org.json for unit tests — the android.jar stub returns nulls under
    // returnDefaultValues, which breaks settings-file parsing tests.
    testImplementation("org.json:json:20240303")
    testImplementation(kotlin("reflect"))
    testImplementation("org.objenesis:objenesis:3.3")
    testImplementation(libs.androidx.core.testing)
    // Explicitly include SDK for unit test classpath (IDE test runner compatibility)
    testImplementation(libs.chat.sdk.android)
    testImplementation(libs.calls.sdk.android)

    // Android instrumented testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation("org.mockito:mockito-android:5.21.0")
}
