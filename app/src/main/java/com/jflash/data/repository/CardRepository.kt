package com.jflash.data.repository

import com.jflash.data.database.dao.CardDao
import com.jflash.data.database.entity.CardEntity
import com.jflash.data.model.CardType
import com.jflash.domain.model.Card
import com.jflash.domain.model.FSRSState
import com.jflash.domain.usecase.FSRSAlgorithm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardRepository @Inject constructor(
    private val cardDao: CardDao,
    private val fsrsAlgorithm: FSRSAlgorithm
) {
    fun getCardsByList(listId: Long): Flow<List<Card>> {
        return cardDao.getCardsByList(listId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getCardsByListId(listId: String): List<Card> {
        return cardDao.getCardsByList(listId.toLong()).map { entities ->
            entities.map { it.toDomainModel() }
        }.first()
    }

    suspend fun getNextDueCard(listId: Long): Card? {
        return cardDao.getNextDueCard(listId)?.toDomainModel()
    }

    suspend fun getDueCardCount(listId: Long): Int {
        return cardDao.getDueCardCount(listId)
    }

    suspend fun getCardById(id: Long): Card? {
        return cardDao.getCardById(id)?.toDomainModel()
    }

    suspend fun createCard(
        listId: Long,
        japanese: String,
        reading: String,
        meaning: String,
        cardType: CardType,
        japaneseDbRef: Long? = null
    ): Long {
        return cardDao.insertCard(
            CardEntity(
                listId = listId,
                japanese = japanese,
                reading = reading,
                meaning = meaning,
                cardType = cardType,
                japaneseDbRef = japaneseDbRef
            )
        )
    }

    suspend fun createCards(cards: List<CardEntity>) {
        cardDao.insertCards(cards)
    }

    suspend fun updateCard(card: Card) {
        cardDao.updateCard(card.toEntity())
    }

    suspend fun deleteCard(card: Card) {
        cardDao.deleteCard(card.toEntity())
    }

    suspend fun getExistingRefs(listId: Long): List<Long> {
        return cardDao.getExistingRefs(listId)
    }

    suspend fun deleteCardsByRefs(listId: Long, refs: List<Long>) {
        cardDao.deleteCardsByRefs(listId, refs)
    }

    private fun CardEntity.toDomainModel(): Card {
        val fsrsState = FSRSState(
            difficulty = fsrsDifficulty,
            stability = fsrsStability,
            elapsedDays = fsrsElapsedDays,
            scheduledDays = fsrsScheduledDays,
            reps = fsrsReps,
            lapses = fsrsLapses,
            state = fsrsState,
            lastReview = fsrsLastReview
        )
        
        return Card(
            id = id,
            listId = listId,
            japanese = japanese,
            reading = reading,
            meaning = meaning,
            cardType = cardType,
            lastShownAt = lastShownAt,
            nextDueAt = nextDueAt,
            fsrsState = fsrsState,
            createdAt = createdAt,
            updatedAt = updatedAt,
            percentLearned = fsrsAlgorithm.calculatePercentLearned(fsrsState)
        )
    }

    private fun Card.toEntity() = CardEntity(
        id = id,
        listId = listId,
        japanese = japanese,
        reading = reading,
        meaning = meaning,
        cardType = cardType,
        lastShownAt = lastShownAt,
        nextDueAt = nextDueAt,
        fsrsDifficulty = fsrsState.difficulty,
        fsrsStability = fsrsState.stability,
        fsrsElapsedDays = fsrsState.elapsedDays,
        fsrsScheduledDays = fsrsState.scheduledDays,
        fsrsReps = fsrsState.reps,
        fsrsLapses = fsrsState.lapses,
        fsrsState = fsrsState.state,
        fsrsLastReview = fsrsState.lastReview,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}