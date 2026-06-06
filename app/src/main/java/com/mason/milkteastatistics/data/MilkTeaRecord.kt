package com.mason.milkteastatistics.data

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
