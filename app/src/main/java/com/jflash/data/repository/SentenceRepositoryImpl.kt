package com.jflash.data.repository

import com.jflash.data.database.JapaneseDbHelper
import com.jflash.domain.model.Sentence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SentenceRepositoryImpl @Inject constructor(
    private val japaneseDbHelper: JapaneseDbHelper
) : SentenceRepository {
    
    override fun searchSentences(searchTerm: String): Flow<List<Sentence>> = flow {
        val sentences = japaneseDbHelper.searchSentences(searchTerm)
        emit(sentences)
    }.flowOn(Dispatchers.IO)
}