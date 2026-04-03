import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

ext["publishArtifactId"] = "chatuikit-kotlin-android"
ext["publishDescription"] = "CometChat UI Kit Kotlin – Android Views/XML chat UI components"
val libraryVersion = System.getenv("LIBRARY_VERSION") ?: "6.0.0-beta.1"
val libraryGroup = "com.cometchat"
val libraryArtifact = "chatuikit-kotlin-android"
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
            all {
                it.useJUnitPlatform()
            }
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
        register<MavenPublication>("chatuikitkotlin") {
            groupId = libraryGroup
            artifactId = libraryArtifact
            version = libraryVersion

            artifact("${layout.buildDirectory.get()}/outputs/aar/chatuikit-kotlin-release.aar")

            pom {
                name.set("CometChatUIKitKotlin")
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
    // Core module – shared ViewModels and business logic (published artifact)
    implementation(libs.chatuikit.core.android)

//    implementation(project(":chatuikit-core"))

    // CometChat SDK
    implementation(libs.chat.sdk.android)
    compileOnly(libs.calls.sdk.android)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

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
    testImplementation("androidx.test:core:1.5.0")
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")

    // Android instrumented testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.rules)
}
