package com.mason.milkteastatistics.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MilkTeaRecord::class], version = 1, exportSchema = false)
abstract class MilkTeaDatabase : RoomDatabase() {

    abstract fun milkTeaDao(): MilkTeaDao

    companion object {
        @Volatile
        private var INSTANCE: MilkTeaDatabase? = null

        fun getDatabase(context: Context): MilkTeaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MilkTeaDatabase::class.java,
                    "milk_tea_database",
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
