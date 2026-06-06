package com.mason.milkteastatistics.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "common_brands",
    indices = [Index(value = ["name"], unique = true)],
)
data class CommonBrand(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
)
