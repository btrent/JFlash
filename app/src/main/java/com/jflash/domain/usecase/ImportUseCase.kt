package com.jflash.domain.usecase

import android.content.Context
import android.net.Uri
import com.jflash.data.database.JapaneseDbHelper
import com.jflash.data.database.entity.CardEntity
import com.jflash.data.model.CardType
import com.jflash.data.model.ImportData
import com.jflash.data.repository.CardRepository
import com.jflash.data.repository.ListRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream
import javax.inject.Inject

class ImportUseCase @Inject constructor(
    private val context: Context,
    private val listRepository: ListRepository,
    private val cardRepository: CardRepository
) {
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun importFromUri(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ImportResult.Error("Could not open file")

            val gzipStream = GZIPInputStream(inputStream)
            val reader = BufferedReader(InputStreamReader(gzipStream))
            val jsonContent = reader.use { it.readText() }

            val importData = json.decodeFromString<ImportData>(jsonContent)
            
            importLists(importData)
            
            ImportResult.Success
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Import failed")
        }
    }

    private suspend fun importLists(importData: ImportData) {
        val japaneseDb = JapaneseDbHelper(context)
        
        try {
            for (importList in importData.lists) {
                val existingList = listRepository.getListByTitle(importList.title)
                val listId = if (existingList != null) {
                    // Sync existing list
                    syncList(existingList.id, importList, japaneseDb)
                    existingList.id
                } else {
                    // Create new list
                    val newListId = listRepository.createList(importList.title)
                    importAllEntries(newListId, importList, japaneseDb)
                    newListId
                }
            }
        } finally {
            japaneseDb.close()
        }
    }

    private suspend fun syncList(listId: Long, importList: com.jflash.data.model.ImportList, japaneseDb: JapaneseDbHelper) {
        val existingRefs = cardRepository.getExistingRefs(listId).toSet()
        val importRefs = importList.entries.map { it.ref }.toSet()
        
        // Remove cards that are no longer in the import
        val refsToRemove = existingRefs.minus(importRefs)
        if (refsToRemove.isNotEmpty()) {
            cardRepository.deleteCardsByRefs(listId, refsToRemove.toList())
        }
        
        // Add new cards
        val refsToAdd = importRefs.minus(existingRefs)
        val cardsToAdd = mutableListOf<CardEntity>()
        
        for (ref in refsToAdd) {
            val entry = japaneseDb.queryEntry(ref) ?: continue
            cardsToAdd.addAll(createCardsFromEntry(listId, entry, ref))
        }
        
        if (cardsToAdd.isNotEmpty()) {
            cardRepository.createCards(cardsToAdd)
        }
    }

    private suspend fun importAllEntries(listId: Long, importList: com.jflash.data.model.ImportList, japaneseDb: JapaneseDbHelper) {
        val cards = mutableListOf<CardEntity>()
        
        for (importEntry in importList.entries) {
            val entry = japaneseDb.queryEntry(importEntry.ref) ?: continue
            cards.addAll(createCardsFromEntry(listId, entry, importEntry.ref))
        }
        
        if (cards.isNotEmpty()) {
            cardRepository.createCards(cards)
        }
    }

    private fun createCardsFromEntry(listId: Long, entry: com.jflash.data.database.JapaneseEntry, ref: Long): List<CardEntity> {
        val japanese = entry.entry
        val reading = entry.furigana ?: entry.entry
        val meaning = entry.summary ?: "(no meaning in db)"
        
        return if (entry.furigana == null || entry.furigana == entry.entry) {
            // Tuple case: (JP, EN)
            listOf(
                CardEntity(
                    listId = listId,
                    japanese = japanese,
                    reading = reading,
                    meaning = meaning,
                    cardType = CardType.JP_TO_EN,
                    japaneseDbRef = ref
                ),
                CardEntity(
                    listId = listId,
                    japanese = japanese,
                    reading = reading,
                    meaning = meaning,
                    cardType = CardType.EN_TO_JP,
                    japaneseDbRef = ref
                )
            )
        } else {
            // Triple case: (JP, READING, EN)
            listOf(
                CardEntity(
                    listId = listId,
                    japanese = japanese,
                    reading = reading,
                    meaning = meaning,
                    cardType = CardType.JP_TO_READING,
                    japaneseDbRef = ref
                ),
                CardEntity(
                    listId = listId,
                    japanese = japanese,
                    reading = reading,
                    meaning = meaning,
                    cardType = CardType.JP_TO_EN,
                    japaneseDbRef = ref
                ),
                CardEntity(
                    listId = listId,
                    japanese = japanese,
                    reading = reading,
                    meaning = meaning,
                    cardType = CardType.EN_TO_JP_READING,
                    japaneseDbRef = ref
                ),
                CardEntity(
                    listId = listId,
                    japanese = japanese,
                    reading = reading,
                    meaning = meaning,
                    cardType = CardType.READING_TO_JP_EN,
                    japaneseDbRef = ref
                )
            )
        }
    }

    sealed class ImportResult {
        object Success : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}