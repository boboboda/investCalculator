import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.google.services)
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
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bobodroid.myapplication"
        minSdk = 26
        targetSdk = 34
        versionCode = 40
        versionName = "26.1.1"
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
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            // 디버그 설정
        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("emulator") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://buyoungsil.ddns.net:3000\"")
            buildConfigField("String", "WEBSOCKET_URL", "\"http://buyoungsil.ddns.net:4200\"")
        }
        create("device") {
            dimension = "environment"
            buildConfigField("String", "WEBSOCKET_URL", "\"http://192.168.0.66:4200\"")
            buildConfigField("String", "BASE_URL", "\"http://192.168.0.66:3000\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://buyoungsil.ddns.net:3000\"")
            buildConfigField("String", "WEBSOCKET_URL", "\"http://buyoungsil.ddns.net:4200\"")
        }
        create("mac") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"http://192.168.166.213:3000\"")
            buildConfigField("String", "WEBSOCKET_URL", "\"http://192.168.166.213:4200\"")
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
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
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
    implementation(libs.kotlin.bom)
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
    implementation(libs.compose.runtime.livedata.actual)

    // Google
    implementation(libs.material)
    implementation(libs.play.services.ads.lite)
    implementation(libs.firebase.crashlytics.buildtools)

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
}