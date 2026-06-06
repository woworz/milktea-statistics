package com.mason.milkteastatistics.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mason.milkteastatistics.data.DailyStats
import com.mason.milkteastatistics.data.DailySummary
import com.mason.milkteastatistics.data.MilkTeaDatabase
import com.mason.milkteastatistics.data.MilkTeaRecord
import com.mason.milkteastatistics.data.MilkTeaRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class DateRange(val label: String) {
    THIS_WEEK("本周"),
    THIS_MONTH("本月"),
    LAST_MONTH("上月"),
}

class MilkTeaViewModel(application: Application) : AndroidViewModel(application) {

    // 先初始化 repository，后续所有属性初始化都可以引用它
    private val repository: MilkTeaRepository = MilkTeaRepository(
        MilkTeaDatabase.getDatabase(application).milkTeaDao(),
    )

    // ========== 筛选状态 ==========

    private val _selectedBrand = MutableStateFlow<String?>(null)
    val selectedBrand: StateFlow<String?> = _selectedBrand.asStateFlow()

    private val _selectedDateRange = MutableStateFlow(DateRange.THIS_MONTH)
    val selectedDateRange: StateFlow<DateRange> = _selectedDateRange.asStateFlow()

    // ========== 品牌列表 ==========

    val allBrands: StateFlow<List<String>> = repository.getAllBrands()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========== 日期范围计算 ==========

    private val dateRangeMillis: StateFlow<Pair<Long, Long>> = _selectedDateRange
        .map { it.toMillis() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DateRange.THIS_MONTH.toMillis(),
        )

    private fun DateRange.toMillis(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (this) {
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

    // ========== 筛选后的记录 ==========

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredRecords: StateFlow<List<MilkTeaRecord>> = dateRangeMillis
        .combine(_selectedBrand) { range, brand -> range to brand }
        .flatMapLatest { (range, brand) ->
            val (start, end) = range
            if (brand != null) {
                repository.getRecordsByDateRangeAndBrand(start, end, brand)
            } else {
                repository.getRecordsByDateRange(start, end)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========== 今日统计（顶部保留） ==========

    private val todayStart = getStartOfToday()

    val todayCount: StateFlow<Int> = repository.getCountForDay(todayStart, todayStart + 86_400_000L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val todayRecords: StateFlow<List<MilkTeaRecord>> = repository.getRecordsForDay(todayStart, todayStart + 86_400_000L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========== 筛选范围内的统计 ==========

    @OptIn(ExperimentalCoroutinesApi::class)
    val stats: StateFlow<DailyStats> = dateRangeMillis
        .combine(_selectedBrand) { range, brand -> range to brand }
        .flatMapLatest { (range, brand) ->
            val (start, end) = range
            if (brand != null) {
                repository.getStatsByBrand(start, end, brand)
            } else {
                repository.getStats(start, end)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailyStats())

    // ========== 趋势数据 ==========

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyAggregates: StateFlow<List<DailySummary>> = dateRangeMillis
        .combine(_selectedBrand) { range, brand -> range to brand }
        .flatMapLatest { (range, brand) ->
            val (start, end) = range
            if (brand != null) {
                repository.getDailyAggregatesByBrand(start, end, brand)
            } else {
                repository.getDailyAggregates(start, end)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ========== 编辑状态 ==========

    private val _editingRecord = MutableStateFlow<MilkTeaRecord?>(null)
    val editingRecord: StateFlow<MilkTeaRecord?> = _editingRecord.asStateFlow()

    // ========== 操作 ==========

    fun addRecord(brand: String, price: Double) {
        viewModelScope.launch {
            repository.insert(
                MilkTeaRecord(
                    timestamp = System.currentTimeMillis(),
                    brand = brand,
                    price = price,
                ),
            )
        }
    }

    fun updateRecord(record: MilkTeaRecord) {
        viewModelScope.launch {
            repository.update(record)
        }
    }

    fun deleteRecord(record: MilkTeaRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
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

    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
