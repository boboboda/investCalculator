# ===== 기본 설정 =====
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# ===== 앱 전체 보호 =====
-keep class com.bobodroid.myapplication.** { *; }
-keepclassmembers class com.bobodroid.myapplication.** { *; }

# ===== Kakao SDK =====
-keep class com.kakao.sdk.** { *; }
-keep interface com.kakao.sdk.** { *; }
-dontwarn com.kakao.sdk.**

# ===== Google & Play Services =====
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ===== Firebase =====
-keep class com.google.firebase.** { *; }
-keep class com.google.firebase.messaging.** { *; }
-keep class com.google.firebase.iid.** { *; }
-dontwarn com.google.firebase.**

# ===== Retrofit & OkHttp =====
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ===== Moshi =====
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
    @com.squareup.moshi.Json <fields>;
}

# ===== Coroutines =====
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ===== Hilt =====
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
}

# ===== Room =====
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# ===== Compose =====
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ===== Billing =====
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ===== Socket.IO =====
-keep class io.socket.** { *; }
-dontwarn io.socket.**

# ===== MPAndroidChart =====
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# ===== Kotlin =====
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ===== ViewModels =====
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# ===== WorkManager =====
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}

# ===== Enum & Parcelable =====
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ===== 경고 무시 (마지막에 추가) =====
-dontwarn javax.**
-dontwarn sun.misc.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn com.squareup.**
-dontwarn kotlin.**
-dontwarn kotlinx.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.common.**
-dontwarn org.codehaus.mojo.animal_sniffer.**

# ===== 로그 제거 =====
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}