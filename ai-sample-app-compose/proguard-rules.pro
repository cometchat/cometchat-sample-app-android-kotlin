# CometChat
-keep class com.cometchat.** { *; }
-dontwarn com.cometchat.**

# Gson
-keep class com.google.gson.** { *; }

# Kotlin
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# App classes
-keep class com.cometchat.ai.sampleapp.compose.** { *; }
