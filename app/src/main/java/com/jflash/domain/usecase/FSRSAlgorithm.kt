package com.jflash.domain.usecase

import com.jflash.domain.model.*
import java.util.Date
import kotlin.math.*

class FSRSAlgorithm(
    private val parameters: FSRSParameters = FSRSParameters()
) {
    
    data class SchedulingInfo(
        val interval: Int,
        val nextReview: Date,
        val newState: FSRSState
    )
    
    fun schedule(
        currentState: FSRSState,
        grade: FSRSGrade,
        now: Date = Date()
    ): SchedulingInfo {
        val elapsedDays = if (currentState.lastReview != null) {
            ((now.time - currentState.lastReview.time) / (1000 * 60 * 60 * 24)).toInt()
        } else {
            0
        }
        
        val interval = calculateInterval(currentState, grade)
        
        val newState = when (grade) {
            FSRSGrade.AGAIN -> handleForgot(currentState, elapsedDays)
            else -> handleRecall(currentState, grade, elapsedDays)
        }
        val nextReview = Date(now.time + interval * 24 * 60 * 60 * 1000L)
        
        return SchedulingInfo(
            interval = interval,
            nextReview = nextReview,
            newState = newState.copy(
                lastReview = now,
                scheduledDays = interval,
                elapsedDays = elapsedDays
            )
        )
    }
    
    private fun handleForgot(state: FSRSState, elapsedDays: Int): FSRSState {
        val newLapses = state.lapses + 1
        val newDifficulty = min(10.0, state.difficulty + parameters.w[14] * (1 - parameters.lapseBonus))
        val newStability = max(0.1, parameters.w[10] * exp(-0.5 * newLapses) * 
            (state.stability + 1) * parameters.w[16])
        
        return state.copy(
            difficulty = newDifficulty,
            stability = newStability,
            lapses = newLapses,
            reps = state.reps + 1,
            state = if (state.state == ReviewState.NEW) ReviewState.LEARNING else ReviewState.RELEARNING,
            elapsedDays = elapsedDays
        )
    }
    
    private fun handleRecall(state: FSRSState, grade: FSRSGrade, elapsedDays: Int): FSRSState {
        val retrievability = if (elapsedDays > 0 && state.stability > 0) {
            exp(-elapsedDays / state.stability)
        } else {
            1.0
        }
        
        val g = grade.value
        val newDifficulty = state.difficulty + parameters.w[6] * (g - 3)
        val clampedDifficulty = max(1.0, min(10.0, newDifficulty))
        
        val s = state.stability
        val r = retrievability
        val d = clampedDifficulty
        
        val hardPenalty = if (grade == FSRSGrade.HARD) parameters.w[15] else 1.0
        val easyBonus = if (grade == FSRSGrade.EASY) parameters.w[16] else 1.0
        
        val newStability = s * (1 + exp(parameters.w[8]) *
            (11 - d) *
            s.pow(-parameters.w[9]) *
            (exp((1 - r) * parameters.w[10]) - 1) *
            hardPenalty *
            easyBonus)
        
        val newState = when (state.state) {
            ReviewState.NEW -> ReviewState.LEARNING
            ReviewState.LEARNING -> if (grade == FSRSGrade.EASY) ReviewState.REVIEW else ReviewState.LEARNING
            ReviewState.RELEARNING -> if (grade != FSRSGrade.HARD) ReviewState.REVIEW else ReviewState.RELEARNING
            else -> ReviewState.REVIEW
        }
        
        return state.copy(
            difficulty = clampedDifficulty,
            stability = max(0.1, newStability),
            reps = state.reps + 1,
            state = newState,
            elapsedDays = elapsedDays
        )
    }
    
    private fun calculateInterval(state: FSRSState, grade: FSRSGrade): Int {
        return when (state.state) {
            ReviewState.NEW -> when (grade) {
                FSRSGrade.AGAIN -> 1
                FSRSGrade.HARD -> 5
                FSRSGrade.GOOD -> 10
                FSRSGrade.EASY -> 15
            }
            ReviewState.LEARNING, ReviewState.RELEARNING -> when (grade) {
                FSRSGrade.AGAIN -> 1
                FSRSGrade.HARD -> 1
                FSRSGrade.GOOD -> 1
                FSRSGrade.EASY -> 4
            }
            ReviewState.REVIEW -> {
                val desiredRetention = parameters.requestRetention
                val interval = -state.stability * ln(desiredRetention) / ln(2.0)
                min(parameters.maximumInterval, max(1, interval.roundToInt()))
            }
        }
    }
    
    fun calculatePercentLearned(state: FSRSState): Int {
        return when (state.state) {
            ReviewState.NEW -> 0
            ReviewState.LEARNING -> minOf(25, state.reps * 5)
            ReviewState.RELEARNING -> maxOf(50, 75 - state.lapses * 5)
            ReviewState.REVIEW -> {
                val stabilityFactor = min(1.0, state.stability / 90.0)
                val lapsesPenalty = max(0.0, 1.0 - state.lapses * 0.05)
                (75 + (25 * stabilityFactor * lapsesPenalty)).toInt()
            }
        }
    }
}