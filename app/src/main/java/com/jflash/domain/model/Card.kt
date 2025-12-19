package com.jflash.domain.model

import com.jflash.data.model.CardType
import java.util.Date

data class Card(
    val id: Long,
    val listId: Long,
    val japanese: String,
    val reading: String,
    val meaning: String,
    val cardType: CardType,
    val lastShownAt: Date?,
    val nextDueAt: Date,
    val fsrsState: FSRSState,
    val createdAt: Date,
    val updatedAt: Date,
    val percentLearned: Int
)