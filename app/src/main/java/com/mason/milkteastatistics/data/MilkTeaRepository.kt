package com.mason.milkteastatistics.data

import kotlinx.coroutines.flow.Flow

class MilkTeaRepository(
    private val dao: MilkTeaDao,
    private val commonBrandDao: CommonBrandDao,
) {

    // ========== CRUD ==========

    fun getAllRecords(): Flow<List<MilkTeaRecord>> = dao.getAllRecords()

    fun getRecordsForDay(startOfDay: Long, endOfDay: Long): Flow<List<MilkTeaRecord>> =
        dao.getRecordsForDay(startOfDay, endOfDay)

    fun getCountForDay(startOfDay: Long, endOfDay: Long): Flow<Int> =
        dao.getCountForDay(startOfDay, endOfDay)

    suspend fun insert(record: MilkTeaRecord) = dao.insert(record)

    suspend fun update(record: MilkTeaRecord) = dao.update(record)

    suspend fun delete(record: MilkTeaRecord) = dao.delete(record)

    // ========== 品牌 ==========

    fun getAllBrands(): Flow<List<String>> = dao.getAllBrands()

    // ========== 筛选 ==========

    fun getRecordsByDateRange(start: Long, end: Long): Flow<List<MilkTeaRecord>> =
        dao.getRecordsByDateRange(start, end)

    fun getRecordsByDateRangeAndBrand(
        start: Long,
        end: Long,
        brand: String,
    ): Flow<List<MilkTeaRecord>> = dao.getRecordsByDateRangeAndBrand(start, end, brand)

    // ========== 统计 ==========

    fun getStats(start: Long, end: Long): Flow<DailyStats> =
        dao.getStats(start, end)

    fun getStatsByBrand(start: Long, end: Long, brand: String): Flow<DailyStats> =
        dao.getStatsByBrand(start, end, brand)

    // ========== 趋势 ==========

    fun getDailyAggregates(start: Long, end: Long): Flow<List<DailySummary>> =
        dao.getDailyAggregates(start, end)

    fun getDailyAggregatesByBrand(
        start: Long,
        end: Long,
        brand: String,
    ): Flow<List<DailySummary>> = dao.getDailyAggregatesByBrand(start, end, brand)

    // ========== 常用品牌 ==========

    fun getCommonBrands(): Flow<List<CommonBrand>> = commonBrandDao.getAll()

    suspend fun addCommonBrand(name: String) {
        commonBrandDao.insert(CommonBrand(name = name))
    }

    suspend fun removeCommonBrand(id: Long) {
        commonBrandDao.deleteById(id)
    }
}
