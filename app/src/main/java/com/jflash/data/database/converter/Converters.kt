package com.jflash.data.database.converter

import androidx.room.TypeConverter
import com.jflash.data.model.CardType
import com.jflash.domain.model.ReviewState
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCardType(cardType: CardType): String {
        return cardType.name
    }

    @TypeConverter
    fun toCardType(cardType: String): CardType {
        return CardType.valueOf(cardType)
    }

    @TypeConverter
    fun fromReviewState(state: ReviewState): String {
        return state.name
    }

    @TypeConverter
    fun toReviewState(state: String): ReviewState {
        return ReviewState.valueOf(state)
    }
}