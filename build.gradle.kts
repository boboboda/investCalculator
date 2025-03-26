buildscript {
    // Compose 버전은 libs.versions.toml에서 관리

    dependencies {
        // 필요한 경우에만 buildscript dependencies 정의
    }
}

// 플러그인들은 이제 libs.versions.toml에서 관리
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.google.services) apply false
}