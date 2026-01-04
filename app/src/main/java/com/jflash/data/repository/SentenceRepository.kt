package com.jflash.data.repository

import com.jflash.domain.model.Sentence
import kotlinx.coroutines.flow.Flow

interface SentenceRepository {
    fun searchSentences(searchTerm: String): Flow<List<Sentence>>
}