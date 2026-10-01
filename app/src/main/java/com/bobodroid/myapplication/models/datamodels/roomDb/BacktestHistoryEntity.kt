package com.bobodroid.myapplication.models.datamodels.roomDb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 환테크 백테스트 시뮬레이터 실행 기록 (로컬 전용, 서버는 결과를 저장하지 않음)
 * responseJson: 서버 BacktestResponse 전체를 Moshi로 직렬화한 원본 — 결과 화면 재진입 시 그대로 복원
 */
@Entity(tableName = "backtest_history")
data class BacktestHistoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "currency_type")
    val currencyType: String,

    @ColumnInfo(name = "period_months")
    val periodMonths: Int,

    @ColumnInfo(name = "invest_strategy")
    val investStrategy: String,

    @ColumnInfo(name = "requested_start_rate")
    val requestedStartRate: Double,

    @ColumnInfo(name = "principal")
    val principal: Double,

    @ColumnInfo(name = "realized_profit_won")
    val realizedProfitWon: Double,

    @ColumnInfo(name = "profit_rate_percent")
    val profitRatePercent: Double,

    @ColumnInfo(name = "buy_count")
    val buyCount: Int,

    @ColumnInfo(name = "sell_count")
    val sellCount: Int,

    @ColumnInfo(name = "response_json")
    val responseJson: String
)
