
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.cometchat.uikit.kotlin"
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all { testTask ->
                testTask.useJUnitPlatform()
                testTask.testLogging {
                    events("passed", "failed", "skipped")
                    showStandardStreams = true
                }
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



configurations.all {
    exclude(group = "org.jetbrains", module = "annotations-java5")
}

dependencies {
    // Core module – shared ViewModels and business logic (published artifact)
    implementation(libs.chatuikit.core.android)
    // implementation(project(":chatuikit-core"))
    // CometChat SDK
    implementation(libs.chat.sdk.android)
    compileOnly(libs.calls.sdk.android)
    implementation(libs.cards.android)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Markwon – Markdown rendering
    implementation("io.noties.markwon:core:4.6.2")
    implementation("io.noties.markwon:ext-strikethrough:4.6.2")
    implementation("io.noties.markwon:ext-tables:4.6.2")
    implementation("io.noties.markwon:html:4.6.2")
    implementation("io.noties.markwon:recycler:4.6.2")
    implementation("io.noties.markwon:recycler-table:4.6.2")
    implementation("io.noties.markwon:syntax-highlight:4.6.2")
    implementation("io.noties:prism4j:2.0.0")

    // Image loading
    implementation(libs.glide)

    // Layout
    implementation(libs.flexbox)
    implementation(libs.gridlayout)

    // Animations
    implementation(libs.lottie)

    // Utilities
    implementation(libs.gson)

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.junit.rule)
    testImplementation(libs.espresso.core)
    testImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.2.1")
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(project(":chatuikit-core"))
    testImplementation(libs.calls.sdk.android)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")

    // Android instrumented testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.mockito.core)
    androidTestImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation("org.mockito:mockito-android:5.21.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.6.1")
    androidTestImplementation("androidx.fragment:fragment-testing:1.8.6")
    androidTestImplementation(project(":chatuikit-core"))
    androidTestImplementation(libs.calls.sdk.android)
    debugImplementation("androidx.fragment:fragment-testing-manifest:1.8.6")
}
