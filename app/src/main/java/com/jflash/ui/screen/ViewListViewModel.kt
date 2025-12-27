package com.jflash.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jflash.data.repository.CardRepository
import com.jflash.data.repository.ListRepository
import com.jflash.domain.model.Card
import com.jflash.domain.model.List as DomainList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewListViewModel @Inject constructor(
    private val cardRepository: CardRepository,
    private val listRepository: ListRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ViewListUiState())
    val uiState: StateFlow<ViewListUiState> = _uiState.asStateFlow()
    
    private val _selectedListId = MutableStateFlow<Long?>(null)
    
    init {
        loadLists()
        observeSelectedListCards()
    }
    
    private fun loadLists() {
        viewModelScope.launch {
            listRepository.getAllLists().collect { lists ->
                _uiState.update { it.copy(lists = lists) }
                if (lists.isNotEmpty() && _selectedListId.value == null) {
                    selectList(lists.first())
                }
            }
        }
    }
    
    private fun observeSelectedListCards() {
        viewModelScope.launch {
            _selectedListId.collect { listId ->
                if (listId != null) {
                    cardRepository.getCardsByList(listId).collect { cards ->
                        val wordGroups = groupCardsByWord(cards)
                        _uiState.update { 
                            it.copy(
                                wordGroups = wordGroups,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }
    
    fun selectList(list: DomainList) {
        _uiState.update { it.copy(selectedList = list, isLoading = true) }
        _selectedListId.value = list.id
    }
    
    fun selectListById(listId: String) {
        viewModelScope.launch {
            val list = listRepository.getListById(listId)
            if (list != null) {
                selectList(list)
            }
        }
    }
    
    private fun groupCardsByWord(cards: List<Card>): List<WordGroup> {
        return cards.groupBy { Triple(it.japanese, it.reading, it.meaning) }
            .map { (key, groupedCards) ->
                WordGroup(
                    japanese = key.first,
                    reading = key.second,
                    meaning = key.third,
                    cardCount = groupedCards.size,
                    averagePercentLearned = if (groupedCards.isNotEmpty()) {
                        groupedCards.map { it.percentLearned }.average().toInt()
                    } else {
                        0
                    }
                )
            }
            .sortedBy { it.japanese }
    }
}

data class ViewListUiState(
    val lists: List<DomainList> = emptyList(),
    val selectedList: DomainList? = null,
    val wordGroups: List<WordGroup> = emptyList(),
    val isLoading: Boolean = false
)

data class WordGroup(
    val japanese: String,
    val reading: String,
    val meaning: String,
    val cardCount: Int,
    val averagePercentLearned: Int
)