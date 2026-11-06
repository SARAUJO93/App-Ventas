# ProGuard / R8 keep rules for Nova POS

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Room Entities and Domain Models
-keep class com.novapos.app.data.local.** { *; }
-keep class com.novapos.app.model.** { *; }

# Hilt / Dagger
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Kotlin Coroutines
-keepclassmembers class kotlinx.coroutines.android.HandlerDispatcher {
    *** INSTANCE;
}

# Firebase & Google Play Services
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# CameraX & ML Kit
-keep class androidx.camera.** { *; }
-keep class com.google.mlkit.** { *; }