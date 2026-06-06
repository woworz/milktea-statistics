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
