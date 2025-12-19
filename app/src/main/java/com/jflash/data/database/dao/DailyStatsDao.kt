package com.jflash.data.database.dao

import androidx.room.*
import com.jflash.data.database.entity.DailyStatsEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM daily_stats WHERE date = :date")
    suspend fun getStatsForDate(date: Date): DailyStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStats(stats: DailyStatsEntity)

    @Query("UPDATE daily_stats SET cardsReviewedToday = cardsReviewedToday + 1 WHERE date = :date")
    suspend fun incrementReviewCount(date: Date)

    @Query("SELECT cardsReviewedToday FROM daily_stats WHERE date = :date")
    suspend fun getReviewCountForDate(date: Date): Int?
}