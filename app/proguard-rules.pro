# ============================================================
# ShadowCore ProGuard Rules
# ============================================================

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Hilt ---
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter

# --- Google Play Billing ---
-keep class com.android.vending.billing.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --- Compose ---
-dontwarn androidx.compose.**

# --- Keep data classes used by Room ---
-keep class com.shadowcore.app.data.local.entity.** { *; }

# --- Keep domain models ---
-keep class com.shadowcore.app.domain.model.** { *; }
