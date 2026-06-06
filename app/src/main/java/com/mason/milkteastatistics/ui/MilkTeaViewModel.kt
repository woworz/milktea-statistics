package com.mason.milkteastatistics.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mason.milkteastatistics.data.MilkTeaDatabase
import com.mason.milkteastatistics.data.MilkTeaRecord
import com.mason.milkteastatistics.data.MilkTeaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MilkTeaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MilkTeaRepository

    val allRecords: StateFlow<List<MilkTeaRecord>>
    val todayCount: StateFlow<Int>
    val todayRecords: StateFlow<List<MilkTeaRecord>>

    init {
        val dao = MilkTeaDatabase.getDatabase(application).milkTeaDao()
        repository = MilkTeaRepository(dao)

        val todayStart = getStartOfToday()
        val todayEnd = todayStart + 86_400_000L // 24 hours in ms

        allRecords = repository.getAllRecords()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

        todayCount = repository.getCountForDay(todayStart, todayEnd)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

        todayRecords = repository.getRecordsForDay(todayStart, todayEnd)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }

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

    fun deleteRecord(record: MilkTeaRecord) {
        viewModelScope.launch {
            repository.delete(record)
        }
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
