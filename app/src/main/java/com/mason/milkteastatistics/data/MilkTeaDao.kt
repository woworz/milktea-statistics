package com.mason.milkteastatistics.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MilkTeaDao {

    @Query("SELECT * FROM milk_tea_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<MilkTeaRecord>>

    @Query(
        """
        SELECT * FROM milk_tea_records 
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay 
        ORDER BY timestamp DESC
        """
    )
    fun getRecordsForDay(startOfDay: Long, endOfDay: Long): Flow<List<MilkTeaRecord>>

    @Query(
        """
        SELECT COUNT(*) FROM milk_tea_records 
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay
        """
    )
    fun getCountForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Insert
    suspend fun insert(record: MilkTeaRecord)

    @Delete
    suspend fun delete(record: MilkTeaRecord)
}
