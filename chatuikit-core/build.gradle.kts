import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    id("maven-publish")
}

ext["publishArtifactId"] = "chatuikit-core-android"
ext["publishDescription"] = "CometChat UI Kit Core – shared ViewModels, use cases, and repositories for Android"
// Get version information from properties or environment
val libraryVersion = System.getenv("LIBRARY_VERSION") ?: "6.0.0-beta.1"
val libraryGroup = "com.cometchat"
val libraryArtifact = "chatuikit-core-android"

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
        register<MavenPublication>("chatuikitcore") {
            groupId = libraryGroup
            artifactId = libraryArtifact
            version = libraryVersion

            artifact("${layout.buildDirectory.get()}/outputs/aar/chatuikit-core-release.aar")

            pom {
                name.set("CometChatUIKitCore")
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
    // CometChat SDKs – exposed to consumers
    api(libs.chat.sdk.android)
    compileOnly(libs.calls.sdk.android)

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

    // Android instrumented testing
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
