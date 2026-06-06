package com.mason.milkteastatistics.data

import kotlinx.coroutines.flow.Flow

class MilkTeaRepository(private val dao: MilkTeaDao) {

    fun getAllRecords(): Flow<List<MilkTeaRecord>> = dao.getAllRecords()

    fun getRecordsForDay(startOfDay: Long, endOfDay: Long): Flow<List<MilkTeaRecord>> =
        dao.getRecordsForDay(startOfDay, endOfDay)

    fun getCountForDay(startOfDay: Long, endOfDay: Long): Flow<Int> =
        dao.getCountForDay(startOfDay, endOfDay)

    suspend fun insert(record: MilkTeaRecord) = dao.insert(record)

    suspend fun delete(record: MilkTeaRecord) = dao.delete(record)
}
