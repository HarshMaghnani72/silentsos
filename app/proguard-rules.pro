# SilentSOS ProGuard Rules

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.silentsos.app.domain.model.** { *; }
-keep class com.google.firebase.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-dontwarn androidx.compose.**

# Keep data classes for Firestore serialization
-keepclassmembers class com.silentsos.app.domain.model.* {
    <init>(...);
    <fields>;
}
