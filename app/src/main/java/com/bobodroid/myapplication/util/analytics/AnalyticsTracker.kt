package com.bobodroid.myapplication.util.analytics

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.bobodroid.myapplication.BuildConfig
import com.bobodroid.myapplication.models.datamodels.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 이벤트 분석 로거.
 * - track() 으로 이벤트를 쌓고, 30초마다 / 20건 쌓이면 / 앱이 백그라운드로 갈 때 홈페이지로 전송한다.
 * - 전송 실패 시 큐에 남겨 두었다가 재전송한다. (이벤트마다 UUID가 있어 서버에서 중복이 걸러진다)
 * - installId 는 기존 deviceId(= localUserData.id)를 그대로 쓴다.
 * - ANALYTICS_KEY 가 비어 있으면 아무것도 하지 않는다.
 */
@Singleton
class AnalyticsTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
) {

    companion object {
        private const val TAG = "AnalyticsTracker"
        private const val FLUSH_INTERVAL_MS = 30_000L
        private const val FLUSH_THRESHOLD = 20
        private const val MAX_BATCH = 50
        private const val MAX_QUEUE = 500
        private const val SESSION_TIMEOUT_MS = 30 * 60 * 1000L
        private const val MAX_BACKOFF_MS = 5 * 60 * 1000L
        private const val QUEUE_FILE = "analytics_queue.json"

        @Volatile
        var instance: AnalyticsTracker? = null
            private set
    }

    private enum class SendResult { OK, DROP, RETRY }

    private val enabled = BuildConfig.ANALYTICS_KEY.isNotBlank()
    private val endpoint = BuildConfig.BASE_URL.trimEnd('/') + "/api/analytics/collect"
    private val appVersion = BuildConfig.VERSION_NAME + if (BuildConfig.DEBUG) "-debug" else ""

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private val queueLock = Any()
    private val queue = ArrayDeque<JSONObject>()
    private val flushMutex = Mutex()

    @Volatile private var started = false
    @Volatile private var sessionId: String? = null
    @Volatile private var lastBackgroundAt = 0L
    @Volatile private var failCount = 0
    @Volatile private var nextAttemptAt = 0L

    // 메인 스레드에서만 접근
    private var startedActivities = 0

    init {
        instance = this
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 시작 (Application.onCreate 에서 1회 호출)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun start(app: Application) {
        if (!enabled) {
            Log.w(TAG, "ANALYTICS_KEY 가 비어 있어 로거가 꺼져 있습니다. (local.properties 의 analytics_key, Gradle Sync, 재빌드 확인)")
            return
        }
        if (started) return
        started = true
        Log.i(TAG, "로거 시작 → $endpoint (appVersion=$appVersion)")

        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                if (startedActivities == 0) onForeground()
                startedActivities++
            }

            override fun onActivityStopped(activity: Activity) {
                startedActivities = (startedActivities - 1).coerceAtLeast(0)
                if (startedActivities == 0) onBackground()
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

        scope.launch {
            loadQueueFromDisk()

            while (isActive) {
                delay(FLUSH_INTERVAL_MS)
                flush()
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 세션
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun onForeground() {
        val now = System.currentTimeMillis()

        // 처음 열었거나, 백그라운드에 30분 넘게 있었다면 새 세션
        if (sessionId == null || now - lastBackgroundAt > SESSION_TIMEOUT_MS) {
            sessionId = UUID.randomUUID().toString()
            Log.i(TAG, "새 세션 시작")
            track("session_start")
        }
    }

    private fun onBackground() {
        lastBackgroundAt = System.currentTimeMillis()

        scope.launch {
            flush()
            persistQueue()
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 이벤트 기록
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    fun track(name: String, params: Map<String, Any?>? = null) {
        if (!enabled) return

        try {
            val event = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("name", name)
                put("ts", System.currentTimeMillis())
                sessionId?.let { put("sessionId", it) }
                // 보낼 때 installId 를 확정하기 위한 내부 필드 (전송 직전에 제거됨)
                currentInstallId()?.let { put("installId", it) }

                if (!params.isNullOrEmpty()) {
                    val p = JSONObject()
                    params.forEach { (k, v) -> p.put(k, v ?: JSONObject.NULL) }
                    put("params", p)
                }
            }

            val size = synchronized(queueLock) {
                queue.addLast(event)
                while (queue.size > MAX_QUEUE) queue.removeFirst()
                queue.size
            }

            if (size >= FLUSH_THRESHOLD) scope.launch { flush() }
        } catch (e: Exception) {
            Log.w(TAG, "이벤트 기록 실패: $name", e)
        }
    }

    private fun currentInstallId(): String? =
        userRepository.userData.value?.localUserData?.id?.toString()

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 전송
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private suspend fun flush() {
        if (!enabled) return

        flushMutex.withLock {
            while (true) {
                if (System.currentTimeMillis() < nextAttemptAt) return

                // 유저 정보가 아직 안 올라왔으면 잠깐 기다린다
                val fallbackId = currentInstallId()
                    ?: withTimeoutOrNull(3_000L) {
                        userRepository.userData.filterNotNull().first().localUserData.id.toString()
                    }
                if (fallbackId == null) {
                    Log.w(TAG, "유저 정보(deviceId)를 아직 못 가져와 전송을 미룹니다.")
                    return
                }

                val batch = peekBatch(fallbackId)
                if (batch.isEmpty()) return

                when (send(batch)) {
                    SendResult.OK, SendResult.DROP -> {
                        synchronized(queueLock) { queue.removeAll(batch) }
                        failCount = 0
                        nextAttemptAt = 0L
                    }

                    SendResult.RETRY -> {
                        failCount += 1
                        nextAttemptAt = System.currentTimeMillis() +
                                minOf(MAX_BACKOFF_MS, 5_000L shl minOf(failCount, 6))
                        persistQueue()
                        return
                    }
                }
            }
        }
    }

    /** 큐 맨 앞에서 installId 가 같은 이벤트를 최대 50건 꺼내 본다 (큐에서는 아직 지우지 않음) */
    private fun peekBatch(fallbackInstallId: String): List<JSONObject> {
        return synchronized(queueLock) {
            queue.forEach { if (!it.has("installId")) it.put("installId", fallbackInstallId) }

            val first = queue.firstOrNull()

            if (first == null) {
                emptyList()
            } else {
                val id = first.getString("installId")
                queue.takeWhile { it.getString("installId") == id }.take(MAX_BATCH)
            }
        }
    }

    private fun send(batch: List<JSONObject>): SendResult {
        return try {
            val events = JSONArray()
            batch.forEach { e ->
                events.put(JSONObject(e.toString()).apply { remove("installId") })
            }

            val body = JSONObject().apply {
                put("installId", batch.first().getString("installId"))
                put("appVersion", appVersion)
                put("platform", "android")
                put("osVersion", Build.VERSION.RELEASE ?: "")
                put("events", events)
            }.toString()

            val request = Request.Builder()
                .url(endpoint)
                .header("x-analytics-key", BuildConfig.ANALYTICS_KEY)
                .post(body.toRequestBody(jsonType))
                .build()

            client.newCall(request).execute().use { res ->
                when {
                    res.isSuccessful -> {
                        Log.i(TAG, "전송 성공: ${batch.size}건")
                        SendResult.OK
                    }
                    // 형식 오류는 다시 보내도 소용없으니 버린다
                    res.code == 400 || res.code == 413 -> {
                        Log.w(TAG, "서버가 거절해 버림: HTTP ${res.code} ${res.body?.string()?.take(200)}")
                        SendResult.DROP
                    }
                    else -> {
                        Log.w(TAG, "전송 실패, 재시도 예정: HTTP ${res.code} ${res.body?.string()?.take(200)}")
                        SendResult.RETRY
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "전송 실패, 나중에 재시도: ${e.javaClass.simpleName} ${e.message}")
            SendResult.RETRY
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━
    // 큐 저장 / 복구 (앱이 종료돼도 못 보낸 이벤트를 살림)
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun persistQueue() {
        try {
            val arr = synchronized(queueLock) {
                JSONArray().also { a -> queue.forEach { a.put(it) } }
            }
            File(context.filesDir, QUEUE_FILE).writeText(arr.toString())
        } catch (e: Exception) {
            Log.w(TAG, "큐 저장 실패", e)
        }
    }

    private fun loadQueueFromDisk() {
        try {
            val file = File(context.filesDir, QUEUE_FILE)
            if (!file.exists()) return

            val arr = JSONArray(file.readText())
            val restored = (0 until arr.length()).mapNotNull { arr.optJSONObject(it) }

            synchronized(queueLock) {
                queue.addAll(0, restored)
                while (queue.size > MAX_QUEUE) queue.removeFirst()
            }
        } catch (e: Exception) {
            Log.w(TAG, "큐 복구 실패", e)
        }
    }
}

/** 어디서든 한 줄로 이벤트를 기록한다. (Hilt 주입이 없는 Composable, 최상위 함수에서도 사용 가능) */
fun trackEvent(name: String, params: Map<String, Any?>? = null) {
    AnalyticsTracker.instance?.track(name, params)
}