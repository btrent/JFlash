package com.jflash.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jflash.data.database.converter.Converters
import com.jflash.data.database.dao.CardDao
import com.jflash.data.database.dao.DailyStatsDao
import com.jflash.data.database.dao.ListDao
import com.jflash.data.database.entity.CardEntity
import com.jflash.data.database.entity.DailyStatsEntity
import com.jflash.data.database.entity.ListEntity

@Database(
    entities = [ListEntity::class, CardEntity::class, DailyStatsEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class JFlashDatabase : RoomDatabase() {
    abstract fun listDao(): ListDao
    abstract fun cardDao(): CardDao
    abstract fun dailyStatsDao(): DailyStatsDao

    companion object {
        @Volatile
        private var INSTANCE: JFlashDatabase? = null

        fun getDatabase(context: Context): JFlashDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JFlashDatabase::class.java,
                    "jflash.db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}