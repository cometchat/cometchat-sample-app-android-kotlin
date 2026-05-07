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

# ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static ** bind(android.view.View);
    public static ** inflate(android.view.LayoutInflater);
}

# App classes
-keep class com.cometchat.ai.sampleapp.kotlin.** { *; }
