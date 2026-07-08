package com.mason.milkteastatistics.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "milk_tea_records")
data class MilkTeaRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val brand: String,
    val drinkName: String? = null,
    val price: Double,
)

/** 日/周/月聚合统计 */
data class DailyStats(
    val totalCount: Int = 0,
    val totalSpend: Double = 0.0,
    val avgPrice: Double = 0.0,
)

/** 单日聚合（用于趋势图） */
data class DailySummary(
    @ColumnInfo(name = "dayStart")
    val dayStart: Long,
    val count: Int,
    val totalSpend: Double,
    val avgPrice: Double,
)

/** 筛选范围内的消费洞察 */
data class ConsumptionInsights(
    val activeDays: Int = 0,
    val averageSpendPerActiveDay: Double = 0.0,
    val averageCupsPerActiveDay: Double = 0.0,
    val topBrand: String? = null,
    val topBrandCount: Int = 0,
    val favoriteDrink: String? = null,
    val favoriteDrinkCount: Int = 0,
    val busiestWeekday: String? = null,
    val busiestWeekdayCount: Int = 0,
    val mostExpensiveRecord: MilkTeaRecord? = null,
    val projectedMonthSpend: Double? = null,
)
