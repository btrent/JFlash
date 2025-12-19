package com.jflash

import com.jflash.domain.model.FSRSGrade
import com.jflash.domain.model.FSRSState
import com.jflash.domain.model.ReviewState
import com.jflash.domain.usecase.FSRSAlgorithm
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date

class FSRSAlgorithmTest {
    
    private lateinit var algorithm: FSRSAlgorithm
    
    @Before
    fun setup() {
        algorithm = FSRSAlgorithm()
    }
    
    @Test
    fun `new card graded as again should move to learning state`() {
        val initialState = FSRSState()
        val result = algorithm.schedule(initialState, FSRSGrade.AGAIN)
        
        assertEquals(ReviewState.LEARNING, result.newState.state)
        assertEquals(1, result.interval)
        assertTrue(result.newState.lapses > initialState.lapses)
    }
    
    @Test
    fun `new card graded as good should move to learning state`() {
        val initialState = FSRSState()
        val result = algorithm.schedule(initialState, FSRSGrade.GOOD)
        
        assertEquals(ReviewState.LEARNING, result.newState.state)
        assertEquals(10, result.interval)
        assertEquals(0, result.newState.lapses)
    }
    
    @Test
    fun `new card graded as easy should move to learning state`() {
        val initialState = FSRSState()
        val result = algorithm.schedule(initialState, FSRSGrade.EASY)
        
        assertEquals(ReviewState.LEARNING, result.newState.state)
        assertEquals(15, result.interval)
        assertEquals(0, result.newState.lapses)
    }
    
    @Test
    fun `learning card graded as easy should move to review state`() {
        val initialState = FSRSState(state = ReviewState.LEARNING, reps = 1)
        val result = algorithm.schedule(initialState, FSRSGrade.EASY)
        
        assertEquals(ReviewState.REVIEW, result.newState.state)
        assertTrue(result.interval >= 1)
    }
    
    @Test
    fun `percentLearned calculation for new card`() {
        val state = FSRSState(state = ReviewState.NEW)
        val percent = algorithm.calculatePercentLearned(state)
        
        assertEquals(0, percent)
    }
    
    @Test
    fun `percentLearned calculation for learning card`() {
        val state = FSRSState(state = ReviewState.LEARNING, reps = 3)
        val percent = algorithm.calculatePercentLearned(state)
        
        assertEquals(15, percent)
    }
    
    @Test
    fun `percentLearned calculation for review card`() {
        val state = FSRSState(state = ReviewState.REVIEW, stability = 30.0, lapses = 0)
        val percent = algorithm.calculatePercentLearned(state)
        
        assertTrue(percent in 75..100)
    }
    
    @Test
    fun `multiple reviews increase stability`() {
        var state = FSRSState()
        
        // First review - Good
        var result = algorithm.schedule(state, FSRSGrade.GOOD)
        state = result.newState
        
        // Second review - Good
        state = state.copy(lastReview = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))
        result = algorithm.schedule(state, FSRSGrade.GOOD)
        
        assertTrue(result.newState.stability > state.stability)
        assertEquals(2, result.newState.reps)
    }
}