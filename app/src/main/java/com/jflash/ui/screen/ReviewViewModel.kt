package com.jflash.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jflash.data.repository.CardRepository
import com.jflash.data.repository.ListRepository
import com.jflash.data.repository.StatsRepository
import com.jflash.domain.model.Card
import com.jflash.domain.model.FSRSGrade
import com.jflash.domain.model.List as DomainList
import com.jflash.domain.usecase.FSRSAlgorithm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val listRepository: ListRepository,
    private val statsRepository: StatsRepository,
    private val fsrsAlgorithm: FSRSAlgorithm
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()
    
    private val _currentCard = MutableStateFlow<Card?>(null)
    val currentCard: StateFlow<Card?> = _currentCard.asStateFlow()
    
    private val _showAnswer = MutableStateFlow(false)
    val showAnswer: StateFlow<Boolean> = _showAnswer.asStateFlow()
    
    private val _schedulingInfo = MutableStateFlow<FSRSAlgorithm.SchedulingInfo?>(null)
    val schedulingInfo: StateFlow<FSRSAlgorithm.SchedulingInfo?> = _schedulingInfo.asStateFlow()
    
    init {
        loadLists()
        loadStats()
    }
    
    private fun loadLists() {
        viewModelScope.launch {
            listRepository.getAllLists().collect { lists ->
                _uiState.update { it.copy(lists = lists) }
                if (lists.isNotEmpty() && _uiState.value.selectedList == null) {
                    selectList(lists.first())
                }
            }
        }
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            val reviewCount = statsRepository.getTodayReviewCount()
            _uiState.update { it.copy(reviewedToday = reviewCount) }
        }
    }
    
    fun selectList(list: DomainList) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedList = list) }
            loadNextCard()
            updateDueCount()
        }
    }
    
    private suspend fun loadNextCard() {
        _uiState.value.selectedList?.let { list ->
            val nextCard = cardRepository.getNextDueCard(list.id)
            _currentCard.value = nextCard
            _showAnswer.value = false
            _schedulingInfo.value = null
        }
    }
    
    private suspend fun updateDueCount() {
        _uiState.value.selectedList?.let { list ->
            val dueCount = cardRepository.getDueCardCount(list.id)
            _uiState.update { it.copy(dueCount = dueCount) }
        }
    }
    
    fun showAnswer() {
        _showAnswer.value = true
    }
    
    fun gradeCard(grade: FSRSGrade) {
        viewModelScope.launch {
            _currentCard.value?.let { card ->
                // Calculate new scheduling
                val schedulingInfo = fsrsAlgorithm.schedule(card.fsrsState, grade, Date())
                _schedulingInfo.value = schedulingInfo
                
                // Update card with new state
                val updatedCard = card.copy(
                    fsrsState = schedulingInfo.newState,
                    nextDueAt = schedulingInfo.nextReview,
                    lastShownAt = Date(),
                    updatedAt = Date(),
                    percentLearned = fsrsAlgorithm.calculatePercentLearned(schedulingInfo.newState)
                )
                
                cardRepository.updateCard(updatedCard)
                
                // Update stats
                statsRepository.incrementTodayReviewCount()
                loadStats()
                
                // Load next card
                loadNextCard()
                updateDueCount()
            }
        }
    }
}

data class ReviewUiState(
    val lists: List<DomainList> = emptyList(),
    val selectedList: DomainList? = null,
    val reviewedToday: Int = 0,
    val dueCount: Int = 0
)