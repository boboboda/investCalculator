# ===== 기본 설정 =====
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# ===== ⚠️ 1단계에서 제거함: 앱 전체 보호 =====
# -keep class com.bobodroid.myapplication.** { *; }
# -keepclassmembers class com.bobodroid.myapplication.** { *; }

# ===== Moshi 리플렉션 기반 모델 보호 (1단계에서 재추가, 유지) =====
-keep class com.bobodroid.myapplication.models.datamodels.service.** { *; }
-keepclassmembers class com.bobodroid.myapplication.models.datamodels.service.** { *; }

# ===== Kakao SDK ===== (그대로 유지 — 아직 안 건드림)
-keep class com.kakao.sdk.** { *; }
-keep interface com.kakao.sdk.** { *; }
-dontwarn com.kakao.sdk.**

# ===== Google & Play Services =====
# ✅ 2단계: 제거 — GMS는 자체 consumer-rules.pro를 AAR에 내장
# -keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ===== Firebase =====
# ✅ 2단계: 제거 — Firebase도 자체 consumer-rules.pro 내장
# -keep class com.google.firebase.** { *; }
# -keep class com.google.firebase.messaging.** { *; }
# -keep class com.google.firebase.iid.** { *; }
-dontwarn com.google.firebase.**

# ===== Retrofit & OkHttp ===== (그대로 유지)
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ===== Moshi ===== (그대로 유지 - 어노테이션 멤버 keep)
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
    @com.squareup.moshi.Json <fields>;
}

# ===== Coroutines =====
# ✅ 2단계: 제거 — coroutines 라이브러리 자체 consumer-rules.pro가 처리
# -keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ===== Hilt ===== (그대로 유지 — DI 그래프라 건드리면 위험)
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
}

# ===== Room ===== (그대로 유지)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# ===== Compose =====
# ✅ 2단계: 제거 — Compose 컴파일러/런타임이 자체 consumer-rules.pro 내장
# -keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ===== Billing =====
# ✅ 2단계: 제거 — Billing 라이브러리도 자체 consumer-rules.pro 내장
# -keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# ===== Socket.IO ===== (그대로 유지 — 이벤트 리스너 리플렉션 가능성)
-keep class io.socket.** { *; }
-dontwarn io.socket.**

# ===== MPAndroidChart ===== (그대로 유지)
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# ===== Kotlin =====
# ✅ 2단계: 제거 — Metadata만 있으면 대부분 충분
# -keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# ===== ViewModels ===== (그대로 유지)
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# ===== WorkManager ===== (그대로 유지)
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(...);
}

# ===== Enum & Parcelable ===== (그대로 유지)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ===== 경고 무시 ===== (그대로 유지)
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

# ===== 로그 제거 ===== (그대로 유지)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}