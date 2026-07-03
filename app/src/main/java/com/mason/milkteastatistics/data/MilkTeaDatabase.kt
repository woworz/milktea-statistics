package com.mason.milkteastatistics.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [MilkTeaRecord::class, CommonBrand::class],
    version = 3,
    exportSchema = false,
)
abstract class MilkTeaDatabase : RoomDatabase() {

    abstract fun milkTeaDao(): MilkTeaDao
    abstract fun commonBrandDao(): CommonBrandDao

    companion object {
        @Volatile
        private var INSTANCE: MilkTeaDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE milk_tea_records ADD COLUMN drinkName TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS common_brands (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_common_brands_name ON common_brands(name)",
                )
            }
        }

        fun getDatabase(context: Context): MilkTeaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MilkTeaDatabase::class.java,
                    "milk_tea_database",
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
