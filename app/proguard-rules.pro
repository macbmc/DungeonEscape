# ProGuard / R8 Rules for Dungeon Escape

# Keep Jetpack Compose runtime & UI
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Kotlin Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep Game Models and Entities to prevent serialization/reflection issues
-keep class com.dungeonescape.models.** { *; }
-keep class com.dungeonescape.entities.** { *; }
-keep class com.dungeonescape.audio.** { *; }
-keep class com.dungeonescape.engine.** { *; }
-keep class com.dungeonescape.utils.** { *; }

# Allow R8 to remove unused code safely while keeping BuildConfig
-keep class com.dungeonescape.BuildConfig { *; }
