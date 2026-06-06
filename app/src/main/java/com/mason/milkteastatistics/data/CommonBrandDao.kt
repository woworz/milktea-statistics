package com.mason.milkteastatistics.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CommonBrandDao {

    @Query("SELECT * FROM common_brands ORDER BY name ASC")
    fun getAll(): Flow<List<CommonBrand>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(brand: CommonBrand)

    @Query("DELETE FROM common_brands WHERE id = :id")
    suspend fun deleteById(id: Long)
}
