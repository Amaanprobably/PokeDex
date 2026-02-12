# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 1. Gson & Retrofit
# Keeps the field names so Gson can match "name" in JSON to "name" in Kotlin
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*
-keep class com.google.gson.stream.** { *; }
-keep class com.example.pokedexapp.data.** { *; }
-dontwarn retrofit2.**
# 2. Room Database
# Prevents R8 from renaming your database entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao class *
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# 3. CRITICAL: Keep your specific Data Transfer Objects (DTOs)
# This prevents R8 from renaming 'name', 'url', 'sprites', etc.
-keep class com.example.pokedexapp.data.remote.responses.** { *; }
-keep class com.example.pokedexapp.data.remote.dto.** { *; }
-keep class com.example.pokedexapp.data.local.** { *; }

# 4. Koin (Dependency Injection)
-keep class org.koin.core.definition.** { *; }
-keep class org.koin.androidx.viewmodel.** { *; }

# 6. Coroutines (Background Tasks)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembers class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    <init>();
}