# Proguard rules for AssistX

# Keep DataStore Preferences
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences { *; }

# Keep Room entities and DAOs
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

