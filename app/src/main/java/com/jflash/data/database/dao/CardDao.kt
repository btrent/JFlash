package com.jflash.data.database.dao

import androidx.room.*
import com.jflash.data.database.entity.CardEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE listId = :listId ORDER BY nextDueAt")
    fun getCardsByList(listId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE listId = :listId AND nextDueAt <= :now ORDER BY nextDueAt LIMIT 1")
    suspend fun getNextDueCard(listId: Long, now: Date = Date()): CardEntity?

    @Query("SELECT COUNT(*) FROM cards WHERE listId = :listId AND nextDueAt <= :now")
    suspend fun getDueCardCount(listId: Long, now: Date = Date()): Int

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCardById(id: Long): CardEntity?

    @Query("SELECT * FROM cards WHERE listId = :listId AND japaneseDbRef = :ref")
    suspend fun getCardByRef(listId: Long, ref: Long): CardEntity?

    @Query("SELECT japaneseDbRef FROM cards WHERE listId = :listId AND japaneseDbRef IS NOT NULL")
    suspend fun getExistingRefs(listId: Long): List<Long>

    @Insert
    suspend fun insertCard(card: CardEntity): Long

    @Insert
    suspend fun insertCards(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("DELETE FROM cards WHERE listId = :listId AND japaneseDbRef IN (:refs)")
    suspend fun deleteCardsByRefs(listId: Long, refs: List<Long>)

    @Query("DELETE FROM cards WHERE listId = :listId")
    suspend fun deleteAllCardsInList(listId: Long)
}