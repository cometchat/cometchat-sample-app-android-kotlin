
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}


configurations.all {
    exclude(group = "org.jetbrains", module = "annotations-java5")
}


android {
    namespace = "com.cometchat.uikit.compose"
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

    testOptions {
        animationsDisabled = true
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all { testTask ->
                testTask.useJUnitPlatform()
                // Complete separation between unit tests and screenshot tests:
                // - testDebugUnitTest: only unit tests (excludes *ScreenshotTest*)
                // - recordRoborazziDebug / verifyRoborazziDebug: only screenshot tests
                val isRoborazziTask = project.gradle.startParameter.taskNames.any { taskName ->
                    taskName.lowercase().contains("roborazzi")
                }
                if (isRoborazziTask) {
                    testTask.include("**/*ScreenshotTest*")
                } else {
                    testTask.exclude("**/*ScreenshotTest*")
                }
            }
        }
    }
}

roborazzi {
    outputDir.set(rootProject.file("screenshot-gallery"))
}


dependencies {
    // Core module – exposed so consumers get ViewModels transitively (published artifact)
    implementation(libs.chatuikit.core.android)
    // implementation(project(":chatuikit-core"))

    // CometChat SDK
    implementation(libs.chat.sdk.android)
    compileOnly(libs.calls.sdk.android)
    implementation(libs.cards.android)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.lifecycle.process)

    // Compose (BOM-managed)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Android Material (View system) for MaterialCardView used in AI Assistant bubble
    implementation(libs.material)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.video)

    // Utilities
    implementation(libs.gson)

    // Markwon – Markdown rendering (for CometChatAIAssistantBubble)
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation("io.noties.markwon:ext-tables:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")
    implementation("io.noties.markwon:recycler:4.6.2")
    implementation("io.noties.markwon:recycler-table:4.6.2")
    implementation("io.noties.markwon:syntax-highlight:4.6.2")
    implementation("io.noties:prism4j:2.0.0")

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.androidx.lifecycle.runtime.testing)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.calls.sdk.android)
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")

    // Android instrumented testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.calls.sdk.android)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.kotlinx.coroutines.test)

    // Debug-only Compose tooling
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
