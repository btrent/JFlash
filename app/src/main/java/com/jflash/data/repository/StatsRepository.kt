package com.jflash.data.repository

import com.jflash.data.database.dao.DailyStatsDao
import com.jflash.data.database.entity.DailyStatsEntity
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatsRepository @Inject constructor(
    private val dailyStatsDao: DailyStatsDao
) {
    suspend fun getTodayReviewCount(): Int {
        val today = getToday()
        return dailyStatsDao.getReviewCountForDate(today) ?: 0
    }

    suspend fun incrementTodayReviewCount() {
        val today = getToday()
        val stats = dailyStatsDao.getStatsForDate(today)
        if (stats == null) {
            dailyStatsDao.insertOrUpdateStats(
                DailyStatsEntity(
                    date = today,
                    cardsReviewedToday = 1
                )
            )
        } else {
            dailyStatsDao.incrementReviewCount(today)
        }
    }

    private fun getToday(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }
}