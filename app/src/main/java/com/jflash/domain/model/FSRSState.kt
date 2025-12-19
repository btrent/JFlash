package com.jflash.domain.model

import java.util.Date

data class FSRSState(
    val difficulty: Double = 5.0,
    val stability: Double = 1.0,
    val elapsedDays: Int = 0,
    val scheduledDays: Int = 0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val state: ReviewState = ReviewState.NEW,
    val lastReview: Date? = null
)

enum class ReviewState {
    NEW,
    LEARNING,
    REVIEW,
    RELEARNING
}