package com.mason.milkteastatistics.service

import com.mason.milkteastatistics.data.MilkTeaRecord
import com.mason.milkteastatistics.data.MilkTeaRepository
import kotlinx.coroutines.flow.Flow

/**
 * 记录服务：负责饮品记录的增删改查和今日统计。
 *
 * 所有对外暴露的查询均返回 [Flow]，由调用方（通常是 ViewModel）通过
 * [kotlinx.coroutines.flow.stateIn] 转换为 [kotlinx.coroutines.flow.StateFlow]。
 */
class RecordService(private val repository: MilkTeaRepository) {

    fun getTodayRecords(todayStart: Long, todayEnd: Long): Flow<List<MilkTeaRecord>> =
        repository.getRecordsForDay(todayStart, todayEnd)

    fun getTodayCount(todayStart: Long, todayEnd: Long): Flow<Int> =
        repository.getCountForDay(todayStart, todayEnd)

    suspend fun addRecord(record: MilkTeaRecord) = repository.insert(record)

    suspend fun updateRecord(record: MilkTeaRecord) = repository.update(record)

    suspend fun deleteRecord(record: MilkTeaRecord) = repository.delete(record)
}
