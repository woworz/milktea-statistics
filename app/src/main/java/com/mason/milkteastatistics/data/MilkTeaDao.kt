package com.mason.milkteastatistics.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkTeaDao {

    // ========== CRUD ==========

    @Query("SELECT * FROM milk_tea_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<MilkTeaRecord>>

    @Query(
        """
        SELECT * FROM milk_tea_records 
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay 
        ORDER BY timestamp DESC
        """,
    )
    fun getRecordsForDay(startOfDay: Long, endOfDay: Long): Flow<List<MilkTeaRecord>>

    @Query(
        """
        SELECT COUNT(*) FROM milk_tea_records 
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay
        """,
    )
    fun getCountForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Insert
    suspend fun insert(record: MilkTeaRecord)

    @Update
    suspend fun update(record: MilkTeaRecord)

    @Delete
    suspend fun delete(record: MilkTeaRecord)

    // ========== 品牌 ==========

    @Query("SELECT DISTINCT brand FROM milk_tea_records ORDER BY brand ASC")
    fun getAllBrands(): Flow<List<String>>

    // ========== 筛选（品牌 + 日期范围） ==========

    @Query(
        """
        SELECT * FROM milk_tea_records 
        WHERE timestamp >= :start AND timestamp < :end 
        ORDER BY timestamp DESC
        """,
    )
    fun getRecordsByDateRange(start: Long, end: Long): Flow<List<MilkTeaRecord>>

    @Query(
        """
        SELECT * FROM milk_tea_records 
        WHERE timestamp >= :start AND timestamp < :end AND brand = :brand 
        ORDER BY timestamp DESC
        """,
    )
    fun getRecordsByDateRangeAndBrand(
        start: Long,
        end: Long,
        brand: String,
    ): Flow<List<MilkTeaRecord>>

    // ========== 统计 ==========

    @Query(
        """
        SELECT 
            COUNT(*) AS totalCount,
            COALESCE(SUM(price), 0) AS totalSpend,
            COALESCE(AVG(price), 0) AS avgPrice
        FROM milk_tea_records 
        WHERE timestamp >= :start AND timestamp < :end
        """,
    )
    fun getStats(start: Long, end: Long): Flow<DailyStats>

    @Query(
        """
        SELECT 
            COUNT(*) AS totalCount,
            COALESCE(SUM(price), 0) AS totalSpend,
            COALESCE(AVG(price), 0) AS avgPrice
        FROM milk_tea_records 
        WHERE timestamp >= :start AND timestamp < :end AND brand = :brand
        """,
    )
    fun getStatsByBrand(start: Long, end: Long, brand: String): Flow<DailyStats>

    // ========== 趋势（每日聚合） ==========

    @Query(
        """
        SELECT 
            (timestamp / 86400000) * 86400000 AS dayStart,
            COUNT(*) AS count,
            COALESCE(SUM(price), 0) AS totalSpend,
            COALESCE(AVG(price), 0) AS avgPrice
        FROM milk_tea_records 
        WHERE timestamp >= :start AND timestamp < :end
        GROUP BY dayStart 
        ORDER BY dayStart ASC
        """,
    )
    fun getDailyAggregates(start: Long, end: Long): Flow<List<DailySummary>>

    @Query(
        """
        SELECT 
            (timestamp / 86400000) * 86400000 AS dayStart,
            COUNT(*) AS count,
            COALESCE(SUM(price), 0) AS totalSpend,
            COALESCE(AVG(price), 0) AS avgPrice
        FROM milk_tea_records 
        WHERE timestamp >= :start AND timestamp < :end AND brand = :brand
        GROUP BY dayStart 
        ORDER BY dayStart ASC
        """,
    )
    fun getDailyAggregatesByBrand(
        start: Long,
        end: Long,
        brand: String,
    ): Flow<List<DailySummary>>
}
