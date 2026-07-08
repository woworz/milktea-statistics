package com.mason.milkteastatistics.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mason.milkteastatistics.data.ConsumptionInsights
import com.mason.milkteastatistics.data.DailyStats
import com.mason.milkteastatistics.data.DailySummary
import com.mason.milkteastatistics.data.MilkTeaDatabase
import com.mason.milkteastatistics.data.CommonBrand
import com.mason.milkteastatistics.data.MilkTeaRecord
import com.mason.milkteastatistics.data.MilkTeaRepository
import com.mason.milkteastatistics.model.DateRange
import com.mason.milkteastatistics.service.AnalyticsService
import com.mason.milkteastatistics.service.BrandService
import com.mason.milkteastatistics.service.RecordService
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MilkTeaViewModel(application: Application) : AndroidViewModel(application) {

    // 依赖层：通过 App Startup 在 Application 启动阶段已完成数据库初始化，
    // 此处 getDatabase() 仅获取已就绪的单例，无阻塞耗时。
    private val db = MilkTeaDatabase.getDatabase(application)
    private val repository: MilkTeaRepository = MilkTeaRepository(
        db.milkTeaDao(),
        db.commonBrandDao(),
    )

    // 业务服务层
    private val recordService = RecordService(repository)
    private val brandService = BrandService(repository)
    private val analyticsService = AnalyticsService(repository)

    // ========== 筛选状态 ==========

    private val _selectedBrand = MutableStateFlow<String?>(null)
    val selectedBrand: StateFlow<String?> = _selectedBrand.asStateFlow()

    private val _selectedDateRange = MutableStateFlow(DateRange.THIS_MONTH)
    val selectedDateRange: StateFlow<DateRange> = _selectedDateRange.asStateFlow()

    // ========== 品牌列表 ==========

    val allBrands: StateFlow<List<String>> = brandService.getAllBrands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========== 常用品牌 ==========

    val commonBrands: StateFlow<List<CommonBrand>> = brandService.getCommonBrands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCommonBrand(name: String) {
        viewModelScope.launch { brandService.addCommonBrand(name) }
    }

    fun removeCommonBrand(id: Long) {
        viewModelScope.launch { brandService.removeCommonBrand(id) }
    }

    // ========== 日期范围计算 ==========

    private val dateRangeMillis: StateFlow<Pair<Long, Long>> = _selectedDateRange
        .map { analyticsService.toMillis(it) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            analyticsService.toMillis(DateRange.THIS_MONTH),
        )

    // ========== 筛选后的记录 ==========

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredRecords: StateFlow<List<MilkTeaRecord>> = dateRangeMillis
        .combine(_selectedBrand) { range, brand -> range to brand }
        .flatMapLatest { (range, brand) ->
            val (start, end) = range
            analyticsService.getFilteredRecords(start, end, brand)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========== 今日统计（顶部保留） ==========

    private val todayRange: StateFlow<Pair<Long, Long>> = flow {
        while (true) {
            val range = getTodayRange()
            emit(range)
            delay((range.second - System.currentTimeMillis()).coerceAtLeast(60_000L))
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        getTodayRange(),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayCount: StateFlow<Int> = todayRange
        .flatMapLatest { (start, end) -> recordService.getTodayCount(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayRecords: StateFlow<List<MilkTeaRecord>> = todayRange
        .flatMapLatest { (start, end) -> recordService.getTodayRecords(start, end) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========== 筛选范围内的统计 ==========

    @OptIn(ExperimentalCoroutinesApi::class)
    val stats: StateFlow<DailyStats> = dateRangeMillis
        .combine(_selectedBrand) { range, brand -> range to brand }
        .flatMapLatest { (range, brand) ->
            val (start, end) = range
            analyticsService.getStats(start, end, brand)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyStats())

    // ========== 趋势数据 ==========

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyAggregates: StateFlow<List<DailySummary>> = dateRangeMillis
        .combine(_selectedBrand) { range, brand -> range to brand }
        .flatMapLatest { (range, brand) ->
            val (start, end) = range
            analyticsService.getDailyAggregates(start, end, brand)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val insights: StateFlow<ConsumptionInsights> = combine(
        filteredRecords,
        dailyAggregates,
        stats,
        _selectedDateRange,
    ) { records, aggregates, statsValue, range ->
        analyticsService.buildConsumptionInsights(records, aggregates, statsValue, range)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConsumptionInsights())

    // ========== 编辑状态 ==========

    private val _editingRecord = MutableStateFlow<MilkTeaRecord?>(null)
    val editingRecord: StateFlow<MilkTeaRecord?> = _editingRecord.asStateFlow()

    // ========== 操作 ==========

    fun addRecord(
        brand: String,
        drinkName: String?,
        price: Double,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        viewModelScope.launch {
            recordService.addRecord(
                MilkTeaRecord(
                    timestamp = timestamp,
                    brand = brand,
                    drinkName = drinkName,
                    price = price,
                ),
            )
        }
    }

    fun updateRecord(record: MilkTeaRecord) {
        viewModelScope.launch { recordService.updateRecord(record) }
    }

    fun deleteRecord(record: MilkTeaRecord) {
        viewModelScope.launch { recordService.deleteRecord(record) }
    }

    fun setBrandFilter(brand: String?) {
        _selectedBrand.value = brand
    }

    fun setDateRange(range: DateRange) {
        _selectedDateRange.value = range
    }

    fun startEdit(record: MilkTeaRecord) {
        _editingRecord.value = record
    }

    fun cancelEdit() {
        _editingRecord.value = null
    }

    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        return start to calendar.timeInMillis
    }
}
