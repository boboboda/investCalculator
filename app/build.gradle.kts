import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.compose.compiler)
}

// Properties 파일 로드를 위한 함수
fun loadProperties(): Properties {
    val properties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        properties.load(localPropertiesFile.inputStream())
    }
    return properties
}

// 키 값 읽기 또는 기본값 반환
fun getPropertyValue(properties: Properties, key: String, defaultValue: String): String {
    return properties.getProperty(key, defaultValue)
}

android {
    buildFeatures {
        buildConfig = true
    }

    // Properties 로드
    val properties = loadProperties()

    namespace = "com.bobodroid.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bobodroid.myapplication"
        minSdk = 26
        targetSdk = 35
        versionCode = 43
        versionName = "26.1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Room의 schemaLocation 설정
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }

        // 안전하게 Properties 사용
        buildConfigField("String", "BANNER_AD_KEY", "\"${getPropertyValue(properties, "banner_ad_key", "default_value")}\"")
        buildConfigField("String", "BUY_BANNER_AD_KEY", "\"${getPropertyValue(properties, "buy_banner_ad_key", "default_value")}\"")
        buildConfigField("String", "FONT_AD_KEY", "\"${getPropertyValue(properties, "font_ad_key", "default_value")}\"")
        buildConfigField("String", "REWARD_FRONT_AD_KEY", "\"${getPropertyValue(properties, "reward_font_ad_key", "default_value")}\"")
        buildConfigField("String", "REWARD_TARGET_FONT_AD_KEY", "\"${getPropertyValue(properties, "reward_target_font_ad_key", "default_value")}\"")
        buildConfigField("String", "KAKAO_APP_KEY", "\"${getPropertyValue(properties, "kakao_app_key", "")}\"")

        manifestPlaceholders["KAKAO_APP_KEY"] = getPropertyValue(properties, "kakao_app_key", "")
    }

    // ✅ 서명 설정 추가
    signingConfigs {
        create("release") {
            val storeFilePath = getPropertyValue(properties, "RELEASE_STORE_FILE", "")
            if (storeFilePath.isNotEmpty()) {
                storeFile = file(storeFilePath)
                storePassword = getPropertyValue(properties, "RELEASE_STORE_PASSWORD", "")
                keyAlias = getPropertyValue(properties, "RELEASE_KEY_ALIAS", "")
                keyPassword = getPropertyValue(properties, "RELEASE_KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        release {
            // ✅ ProGuard 활성화
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // ✅ Release 서명 사용
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            // 디버그 설정
        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("emulator") {
            dimension = "environment"
            // REST API용 (베이스 URL만, 슬래시 포함)
            buildConfigField("String", "BASE_URL", "\"https://www.buyoungsilcoding.com/\"")

            // Socket.IO용 (베이스 URL만, 슬래시 없음)
            buildConfigField("String", "WEBSOCKET_URL", "\"https://www.buyoungsilcoding.com\"")
        }
        create("device") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://www.buyoungsilcoding.com/\"")
            buildConfigField("String", "WEBSOCKET_URL", "\"https://www.buyoungsilcoding.com\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://www.buyoungsilcoding.com/\"")
            buildConfigField("String", "WEBSOCKET_URL", "\"https://www.buyoungsilcoding.com\"")
        }
        create("mac") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://www.buyoungsilcoding.com/\"")
            buildConfigField("String", "WEBSOCKET_URL", "\"https://www.buyoungsilcoding.com\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }

    kotlinOptions {
        jvmTarget = "18"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeBom.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.viewmodel.compose)
    implementation(libs.androidx.splashscreen)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material)
    implementation(libs.compose.material3.windowsize)
    implementation(libs.compose.runtime)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.runtime.rxjava2)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.compose.material.icons.extended)

    // Google
    implementation(libs.material)
    implementation(libs.play.services.ads.lite)

    // Navigation
    implementation(libs.navigation.compose)
    implementation(libs.accompanist.navigation.animation)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    annotationProcessor(libs.room.compiler)
    kapt(libs.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.analytics.ktx)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    kapt(libs.moshi.kotlin.codegen)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Billing
    implementation(libs.billing.ktx)

    // Socket.io
    implementation(libs.socket.io.client)

    // Chart
    implementation(libs.mp.android.chart)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Hilt Testing
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)

    // Social Login
    implementation(libs.play.services.auth)
    implementation(libs.kakao.sdk.user)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
}