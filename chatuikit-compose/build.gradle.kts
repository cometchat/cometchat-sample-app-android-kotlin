import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

ext["publishArtifactId"] = "chatuikit-compose-android"
ext["publishDescription"] = "CometChat UI Kit Compose – Jetpack Compose chat UI components for Android"
val libraryVersion = System.getenv("LIBRARY_VERSION") ?: "6.0.0-beta.1"
val libraryGroup = "com.cometchat"
val libraryArtifact = "chatuikit-compose-android"

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
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

publishing {
    repositories {

        maven {
            url = uri("$projectDir/distribution")
        }

        maven {
            name = "cloudsmith"
            url = uri("https://api-g.cloudsmith.io/maven/cometchat/cometchat")

            credentials {
                val properties = Properties()
                properties.load(project.rootProject.file("local.properties").inputStream())
                username = properties.getProperty("cloudsmith.username")
                password = properties.getProperty("cloudsmith.apikey")
            }
        }
    }

    publications {
        register<MavenPublication>("chatuikitcompose") {
            groupId = libraryGroup
            artifactId = libraryArtifact
            version = libraryVersion

            artifact("${layout.buildDirectory.get()}/outputs/aar/chatuikit-compose-release.aar")

            pom {
                name.set("CometChatUIKitCompose")
                description.set("CometChat virtual chat builder for Android")
                url.set("https://www.cometchat.com")

                licenses {
                    license {
                        name.set("CometChat License")
                        url.set("https://www.cometchat.com/terms")
                    }
                }

                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")
                    val compileDeps = project.configurations.getByName("implementation").allDependencies
                        .filter { dep ->
                            dep.group != null && dep.version != null &&
                                dep.name != "unspecified" &&
                                dep.javaClass.simpleName != "DefaultSelfResolvingDependency"
                        }
                    compileDeps.forEach { dep ->
                        val depNode = dependenciesNode.appendNode("dependency")
                        depNode.appendNode("groupId", dep.group)
                        depNode.appendNode("artifactId", dep.name)
                        depNode.appendNode("version", dep.version)
                        depNode.appendNode("scope", "compile")
                    }
                }
            }
        }
    }
}


dependencies {
    // Core module – exposed so consumers get ViewModels transitively (published artifact)
    implementation(libs.chatuikit.core.android)

//    implementation(project(":chatuikit-core"))

    // CometChat SDK
    implementation(libs.chat.sdk.android)
    compileOnly(libs.calls.sdk.android)

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

    // Image loading
    implementation(libs.coil.compose)

    // Utilities
    implementation(libs.gson)

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

    // Android instrumented testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    // Debug-only Compose tooling
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
