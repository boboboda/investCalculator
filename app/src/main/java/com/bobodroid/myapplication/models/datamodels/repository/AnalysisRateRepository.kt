package com.bobodroid.myapplication.models.datamodels.repository

import android.util.Log
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRateDailyChange
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.ExchangeRateResponse
import com.bobodroid.myapplication.models.datamodels.service.exchangeRateApi.RateApi
import com.bobodroid.myapplication.models.viewmodels.RateRange
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 분석 화면 데이터 저장소
 * - 5개 API(일/주/3개월/1년 + 전일대비)를 병렬로 호출
 * - 앱 전체에서 하나의 캐시를 공유 (ViewModel이 새로 만들어져도 재사용)
 * - 동시에 여러 곳에서 요청해도 네트워크 호출은 한 번만 실행
 */
@Singleton
class AnalysisRateRepository @Inject constructor() {

    data class Snapshot(
        val daily: List<RateRange>,
        val weekly: List<RateRange>,
        val monthly: List<RateRange>, // 실제로는 "3개월" 범위 데이터
        val yearly: List<RateRange>,
        val latest: ExchangeRateDailyChange?,
        val loadedAt: Long
    )

    private val mutex = Mutex()

    @Volatile
    private var cached: Snapshot? = null

    /** TTL 이내의 캐시가 있으면 반환 (네트워크 호출 없음) */
    fun peekFresh(): Snapshot? {
        val snapshot = cached ?: return null
        return if (System.currentTimeMillis() - snapshot.loadedAt < CACHE_TTL_MS) snapshot else null
    }

    /**
     * 데이터 로드
     * @param force true면 TTL과 관계없이 새로 요청
     */
    suspend fun load(force: Boolean = false): Snapshot = mutex.withLock {
        // 락을 기다리는 동안 다른 호출이 이미 로드했다면 그 결과를 재사용
        if (!force) {
            peekFresh()?.let { return@withLock it }
        }

        val previous = cached
        val (snapshot, allSucceeded) = fetchAll(previous)

        if (allSucceeded) {
            cached = snapshot
        }
        snapshot
    }

    private suspend fun fetchAll(previous: Snapshot?): Pair<Snapshot, Boolean> = coroutineScope {
        val dailyDeferred = async(Dispatchers.IO) { fetchRange("day", 0) }
        val weeklyDeferred = async(Dispatchers.IO) { fetchRange("week", 1) }
        val monthlyDeferred = async(Dispatchers.IO) { fetchRange("month", 2) }
        val yearlyDeferred = async(Dispatchers.IO) { fetchRange("year", 3) }
        val latestDeferred = async(Dispatchers.IO) {
            safeCall("getDailyChange") { RateApi.rateService.getDailyChange() }
        }

        val daily = dailyDeferred.await()
        val weekly = weeklyDeferred.await()
        val monthly = monthlyDeferred.await()
        val yearly = yearlyDeferred.await()
        val latest = latestDeferred.await()

        // 가장 기본이 되는 일별 데이터가 없으면 실패 처리 (이전 캐시가 있으면 그 값으로 대체)
        if (daily.isNullOrEmpty() && previous?.daily.isNullOrEmpty()) {
            throw Exception("환율 데이터를 불러올 수 없습니다.\n인터넷 연결을 확인해주세요.")
        }

        // 일부 요청이 실패하면 마지막으로 성공한 값으로 대체 (화면이 비지 않도록)
        val snapshot = Snapshot(
            daily = daily ?: previous?.daily ?: emptyList(),
            weekly = weekly ?: previous?.weekly ?: emptyList(),
            monthly = monthly ?: previous?.monthly ?: emptyList(),
            yearly = yearly ?: previous?.yearly ?: emptyList(),
            latest = latest ?: previous?.latest,
            loadedAt = System.currentTimeMillis()
        )

        val allSucceeded = daily != null && weekly != null && monthly != null &&
                yearly != null && latest != null

        Log.d(
            TAG("AnalysisRateRepository", "fetchAll"),
            "로드 완료 - Daily: ${snapshot.daily.size}, Weekly: ${snapshot.weekly.size}, " +
                    "Monthly: ${snapshot.monthly.size}, Yearly: ${snapshot.yearly.size}, 전체 성공: $allSucceeded"
        )

        Pair(snapshot, allSucceeded)
    }

    private suspend fun fetchRange(period: String, tabIndex: Int): List<RateRange>? {
        val (startDate, endDate) = rangeDateFromTab(tabIndex)
        val rates = safeCall("getRatesByPeriod($period)") {
            RateApi.rateService.getRatesByPeriod(period, startDate, endDate)
        } ?: return null
        return filterAndMapRates(rates)
    }

    private suspend fun <T> safeCall(name: String, block: suspend () -> T): T? {
        return try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG("AnalysisRateRepository", name), "요청 실패: ${e.message}")
            null
        }
    }

    private fun filterAndMapRates(rates: List<ExchangeRateResponse>): List<RateRange> {
        val filteredRates = rates.fold(mutableListOf<ExchangeRateResponse>()) { acc, current ->
            if (acc.isEmpty() ||
                acc.last().exchangeRates.jpy != current.exchangeRates.jpy ||
                acc.last().exchangeRates.usd != current.exchangeRates.usd
            ) {
                acc.add(current)
            } else {
                acc[acc.lastIndex] = current
            }
            acc
        }

        return filteredRates.map {
            val ratesMap = mapOf(
                "USD" to it.exchangeRates.usd,
                "JPY" to it.exchangeRates.jpy,
                "EUR" to it.exchangeRates.eur,
                "GBP" to it.exchangeRates.gbp,
                "CNY" to it.exchangeRates.cny,
                "AUD" to it.exchangeRates.aud,
                "CAD" to it.exchangeRates.cad,
                "CHF" to it.exchangeRates.chf,
                "HKD" to it.exchangeRates.hkd,
                "SGD" to it.exchangeRates.sgd,
                "NZD" to it.exchangeRates.nzd,
                "THB" to it.exchangeRates.thb
            )

            RateRange(
                rates = ratesMap,
                createAt = it.createAt
            )
        }
    }

    private fun rangeDateFromTab(tabIndex: Int): Pair<String, String> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val todayDate = today.format(formatter)

        val startDate = when (tabIndex) {
            0 -> todayDate
            1 -> today.minusWeeks(1).format(formatter)
            2 -> today.minusMonths(3).format(formatter)
            3 -> today.minusYears(1).format(formatter)
            else -> todayDate
        }

        return Pair(startDate, todayDate)
    }

    companion object {
        // 서버 day/daily-change 캐시(60초)와 같은 주기
        private const val CACHE_TTL_MS = 60_000L
    }
}
