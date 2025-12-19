package com.jflash.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jflash.data.model.CardType
import com.jflash.domain.model.ReviewState
import java.util.Date

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId"), Index("nextDueAt")]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listId: Long,
    val japanese: String,
    val reading: String,
    val meaning: String,
    val cardType: CardType,
    val lastShownAt: Date? = null,
    val nextDueAt: Date = Date(),
    
    // FSRS state fields
    val fsrsDifficulty: Double = 5.0,
    val fsrsStability: Double = 1.0,
    val fsrsElapsedDays: Int = 0,
    val fsrsScheduledDays: Int = 0,
    val fsrsReps: Int = 0,
    val fsrsLapses: Int = 0,
    val fsrsState: ReviewState = ReviewState.NEW,
    val fsrsLastReview: Date? = null,
    
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    
    // Reference to the original entry in Japanese4.db
    val japaneseDbRef: Long? = null
)