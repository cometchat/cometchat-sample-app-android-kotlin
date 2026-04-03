# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================================================
# GENERAL RULES
# ============================================================================

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name in stack traces
-renamesourcefileattribute SourceFile

# Keep annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# ============================================================================
# COMETCHAT SDK RULES
# ============================================================================

# Keep all CometChat classes
-keep class com.cometchat.** { *; }
-dontwarn com.cometchat.**

# Keep CometChat UI Kit classes
-keep class com.cometchat.uikit.** { *; }
-dontwarn com.cometchat.uikit.**

# ============================================================================
# GSON RULES
# ============================================================================

-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep generic type information for Gson
-keepattributes Signature

# ============================================================================
# KOTLIN RULES
# ============================================================================

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ============================================================================
# OKHTTP RULES
# ============================================================================

-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ============================================================================
# GLIDE RULES
# ============================================================================

-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
    *** rewind();
}

# ============================================================================
# MATERIAL DESIGN RULES
# ============================================================================

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ============================================================================
# SAMPLE APP SPECIFIC RULES
# ============================================================================

# Keep sample app models
-keep class com.cometchat.sampleapp.kotlin.** { *; }

# Keep ViewBinding classes
-keep class * implements androidx.viewbinding.ViewBinding {
    public static ** bind(android.view.View);
    public static ** inflate(android.view.LayoutInflater);
}

# ============================================================================
# WEBVIEW RULES (if using WebView with JS)
# ============================================================================

# Uncomment if your project uses WebView with JS
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

