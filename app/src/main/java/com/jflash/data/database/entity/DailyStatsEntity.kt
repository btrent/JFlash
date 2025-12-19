package com.jflash.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey
    val date: Date,
    val cardsReviewedToday: Int = 0
)