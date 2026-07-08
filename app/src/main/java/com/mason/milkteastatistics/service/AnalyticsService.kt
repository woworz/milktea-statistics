package com.mason.milkteastatistics.service

import com.mason.milkteastatistics.data.DailyStats
import com.mason.milkteastatistics.data.DailySummary
import com.mason.milkteastatistics.data.ConsumptionInsights
import com.mason.milkteastatistics.data.MilkTeaRecord
import com.mason.milkteastatistics.data.MilkTeaRepository
import com.mason.milkteastatistics.model.DateRange
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * 分析服务：负责日期范围计算、筛选查询、统计和趋势数据。
 *
 * 所有对外暴露的查询均返回 [Flow]，由调用方通过
 * [kotlinx.coroutines.flow.stateIn] 转换为 [kotlinx.coroutines.flow.StateFlow]。
 */
class AnalyticsService(private val repository: MilkTeaRepository) {

    fun getFilteredRecords(start: Long, end: Long, brand: String?): Flow<List<MilkTeaRecord>> =
        if (brand != null) {
            repository.getRecordsByDateRangeAndBrand(start, end, brand)
        } else {
            repository.getRecordsByDateRange(start, end)
        }

    fun getStats(start: Long, end: Long, brand: String?): Flow<DailyStats> =
        if (brand != null) {
            repository.getStatsByBrand(start, end, brand)
        } else {
            repository.getStats(start, end)
        }

    fun getDailyAggregates(start: Long, end: Long, brand: String?): Flow<List<DailySummary>> =
        if (brand != null) {
            repository.getDailyAggregatesByBrand(start, end, brand)
        } else {
            repository.getDailyAggregates(start, end)
        }

    fun buildConsumptionInsights(
        records: List<MilkTeaRecord>,
        dailyAggregates: List<DailySummary>,
        stats: DailyStats,
        range: DateRange,
    ): ConsumptionInsights {
        if (records.isEmpty()) {
            return ConsumptionInsights()
        }

        val activeDays = dailyAggregates.size.coerceAtLeast(1)
        val topBrand = records.topCountByString { it.brand }
        val favoriteDrink = records
            .mapNotNull { it.drinkName?.trim()?.takeIf(String::isNotBlank) }
            .topCountByString { it }
        val busiestWeekday = records.topCountByInt { record ->
            val cal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
            cal.get(Calendar.DAY_OF_WEEK)
        }
        val mostExpensiveRecord = records.maxByOrNull { it.price }

        return ConsumptionInsights(
            activeDays = activeDays,
            averageSpendPerActiveDay = stats.totalSpend / activeDays,
            averageCupsPerActiveDay = stats.totalCount.toDouble() / activeDays,
            topBrand = topBrand?.first,
            topBrandCount = topBrand?.second ?: 0,
            favoriteDrink = favoriteDrink?.first,
            favoriteDrinkCount = favoriteDrink?.second ?: 0,
            busiestWeekday = busiestWeekday?.first?.toWeekdayLabel(),
            busiestWeekdayCount = busiestWeekday?.second ?: 0,
            mostExpensiveRecord = mostExpensiveRecord,
            projectedMonthSpend = stats.totalSpend.projectMonthSpend(range),
        )
    }

    fun toMillis(range: DateRange): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (range) {
            DateRange.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_MONTH, 7)
                start to cal.timeInMillis
            }
            DateRange.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                start to cal.timeInMillis
            }
            DateRange.LAST_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.add(Calendar.MONTH, -1)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                start to cal.timeInMillis
            }
        }
    }

    private fun <T> List<T>.topCountByString(selector: (T) -> String): Pair<String, Int>? =
        groupingBy(selector)
            .eachCount()
            .maxWithOrNull(compareBy<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            ?.let { it.key to it.value }

    private fun <T> List<T>.topCountByInt(selector: (T) -> Int): Pair<Int, Int>? =
        groupingBy(selector)
            .eachCount()
            .maxWithOrNull(compareBy<Map.Entry<Int, Int>> { it.value }.thenBy { it.key })
            ?.let { it.key to it.value }

    private fun Int.toWeekdayLabel(): String =
        when (this) {
            Calendar.MONDAY -> "周一"
            Calendar.TUESDAY -> "周二"
            Calendar.WEDNESDAY -> "周三"
            Calendar.THURSDAY -> "周四"
            Calendar.FRIDAY -> "周五"
            Calendar.SATURDAY -> "周六"
            Calendar.SUNDAY -> "周日"
            else -> "未知"
        }

    private fun Double.projectMonthSpend(range: DateRange): Double? {
        if (range != DateRange.THIS_MONTH || this <= 0.0) {
            return null
        }

        val cal = Calendar.getInstance()
        val elapsedDays = cal.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return this / elapsedDays * daysInMonth
    }
}
