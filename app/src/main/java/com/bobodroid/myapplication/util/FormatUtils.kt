import com.bobodroid.myapplication.models.datamodels.roomDb.Currencies
import com.bobodroid.myapplication.models.datamodels.roomDb.CurrencyType
import java.math.BigDecimal

// 파일: app/src/main/java/com/bobodroid/myapplication/util/FormatUtils.kt

object FormatUtils {  // ⭐ class → object로 변경

    fun formatRate(rate: BigDecimal): String {
        return "%,.2f".format(rate)
    }

    fun formatCurrency(amount: BigDecimal): String {
        val absAmount = amount.abs()
        val formatted = "%,.0f".format(absAmount)
        return when {
            amount > BigDecimal.ZERO -> "+₩$formatted"
            amount < BigDecimal.ZERO -> "-₩$formatted"
            else -> "₩$formatted"
        }
    }

    fun formatProfitRate(rate: BigDecimal): String {
        return when {
            rate > BigDecimal.ZERO -> "+${rate}%"
            rate < BigDecimal.ZERO -> "${rate}%"
            else -> "0.0%"
        }
    }

    fun formatAmount(amount: BigDecimal, type: CurrencyType): String {
        val currency = Currencies.fromCurrencyType(type)
        val formatted = "%,.2f".format(amount)
        return "${currency.symbol}$formatted"
    }
}