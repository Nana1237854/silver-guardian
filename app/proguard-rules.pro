# Silver Guardian ProGuard / R8 Rules

-keepattributes Signature
-keepattributes *Annotation*

# Keep app model classes (used in reflection-like patterns via MockData constructors)
-keep class com.silverguardian.prototype.models.** { *; }

# Keep Activities (registered in AndroidManifest)
-keep class com.silverguardian.prototype.LoginActivity { *; }
-keep class com.silverguardian.prototype.MainActivity { *; }
-keep class com.silverguardian.prototype.ChatDetailActivity { *; }
-keep class com.silverguardian.prototype.PhotoDetailActivity { *; }
-keep class com.silverguardian.prototype.ChildModeActivity { *; }
-keep class com.silverguardian.prototype.BluetoothActivity { *; }
-keep class com.silverguardian.prototype.CommunityActivity { *; }
-keep class com.silverguardian.prototype.FraudDetailActivity { *; }

# Keep BroadcastReceivers (registered in AndroidManifest)
-keep class com.silverguardian.prototype.reminder.ReminderBroadcastReceiver { *; }
-keep class com.silverguardian.prototype.reminder.BootReceiver { *; }

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl { *; }

# WorkManager — Worker 通过反射实例化，必须保留
-keep class com.silverguardian.prototype.reminder.FraudReminderWorker { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# AMap SDK
-keep class com.amap.api.** { *; }
-dontwarn com.amap.api.**

# Keep BuildConfig (generated)
-keep class com.silverguardian.prototype.BuildConfig { *; }
