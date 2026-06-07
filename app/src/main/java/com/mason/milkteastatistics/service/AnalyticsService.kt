package com.mason.milkteastatistics.service

import com.mason.milkteastatistics.data.DailyStats
import com.mason.milkteastatistics.data.DailySummary
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
}
