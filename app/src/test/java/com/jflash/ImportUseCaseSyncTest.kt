package com.jflash

import android.content.Context
import com.jflash.data.database.JapaneseEntry
import com.jflash.data.database.JapaneseEntryProvider
import com.jflash.data.model.ImportData
import com.jflash.data.model.ImportEntry
import com.jflash.data.model.ImportList
import com.jflash.data.model.ImportMeta
import com.jflash.data.repository.CardRepository
import com.jflash.data.repository.ListRepository
import com.jflash.domain.model.List as DomainList
import com.jflash.domain.usecase.ImportUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.Date

/**
 * Tests for ImportUseCase's duplicate detection logic.
 *
 * These tests verify that:
 * 1. When syncing a list, cards with refs that already exist are not re-added
 * 2. Cards with the same content (but different refs) are not re-added
 * 3. New cards are properly added when they don't exist
 */
class ImportUseCaseSyncTest {

    private lateinit var mockContext: Context
    private lateinit var mockListRepository: ListRepository
    private lateinit var mockCardRepository: CardRepository
    private lateinit var mockEntryProvider: JapaneseEntryProvider
    private lateinit var importUseCase: ImportUseCase

    @Before
    fun setup() {
        mockContext = mock()
        mockListRepository = mock()
        mockCardRepository = mock()
        mockEntryProvider = mock()

        importUseCase = ImportUseCase(
            context = mockContext,
            listRepository = mockListRepository,
            cardRepository = mockCardRepository,
            entryProviderFactory = { mockEntryProvider }
        )
    }

    @Test
    fun `syncList skips cards when ref already exists in database`() = runTest {
        // Given: A list that already exists with one card (ref 100)
        val existingList = DomainList(id = 1L, title = "Test List", createdAt = Date(), updatedAt = Date())
        val importList = ImportList(
            id = "test-id",
            title = "Test List",
            updatedAt = "2024-01-01T00:00:00Z",
            entries = listOf(ImportEntry(ref = 100L, updatedAt = "2024-01-01T00:00:00Z"))
        )

        whenever(mockListRepository.getListByTitle("Test List")).thenReturn(existingList)
        whenever(mockCardRepository.getExistingRefs(1L)).thenReturn(listOf(100L)) // ref 100 already exists
        whenever(mockEntryProvider.queryEntry(any())).thenReturn(
            JapaneseEntry(entry = "日本語", furigana = "にほんご", summary = "Japanese")
        )

        // When: We call importLists
        val importData = ImportData(
            meta = ImportMeta(version = "1.0"),
            lists = listOf(importList)
        )
        importUseCase.importLists(importData)

        // Then: No new cards should be created (ref 100 already exists)
        verify(mockCardRepository, never()).createCards(any())
    }

    @Test
    fun `syncList adds cards when ref does not exist in database`() = runTest {
        // Given: A list that already exists but with no cards
        val existingList = DomainList(id = 1L, title = "Test List", createdAt = Date(), updatedAt = Date())
        val importList = ImportList(
            id = "test-id",
            title = "Test List",
            updatedAt = "2024-01-01T00:00:00Z",
            entries = listOf(ImportEntry(ref = 100L, updatedAt = "2024-01-01T00:00:00Z"))
        )

        whenever(mockListRepository.getListByTitle("Test List")).thenReturn(existingList)
        whenever(mockCardRepository.getExistingRefs(1L)).thenReturn(emptyList()) // no existing refs
        whenever(mockEntryProvider.queryEntry(100L)).thenReturn(
            JapaneseEntry(entry = "日本語", furigana = "にほんご", summary = "Japanese")
        )
        // Card doesn't exist by content either
        whenever(mockCardRepository.cardExists(any(), any(), any(), any(), any())).thenReturn(false)

        // When: We call importLists
        val importData = ImportData(
            meta = ImportMeta(version = "1.0"),
            lists = listOf(importList)
        )
        importUseCase.importLists(importData)

        // Then: Cards should be created
        verify(mockCardRepository).createCards(argThat { cards ->
            cards.isNotEmpty() && cards.all { it.japanese == "日本語" }
        })
    }

    @Test
    fun `syncList skips cards when content already exists with different ref`() = runTest {
        // Given: A list with a new ref (200), but the card content already exists from ref 100
        val existingList = DomainList(id = 1L, title = "Test List", createdAt = Date(), updatedAt = Date())
        val importList = ImportList(
            id = "test-id",
            title = "Test List",
            updatedAt = "2024-01-01T00:00:00Z",
            entries = listOf(ImportEntry(ref = 200L, updatedAt = "2024-01-01T00:00:00Z")) // new ref
        )

        whenever(mockListRepository.getListByTitle("Test List")).thenReturn(existingList)
        whenever(mockCardRepository.getExistingRefs(1L)).thenReturn(listOf(100L)) // ref 100 exists, not 200
        whenever(mockEntryProvider.queryEntry(200L)).thenReturn(
            JapaneseEntry(entry = "日本語", furigana = "にほんご", summary = "Japanese")
        )
        // Content already exists (from ref 100 which had the same word)
        whenever(mockCardRepository.cardExists(
            listId = eq(1L),
            japanese = eq("日本語"),
            reading = eq("にほんご"),
            meaning = eq("Japanese"),
            cardType = any()
        )).thenReturn(true)

        // When: We call importLists
        val importData = ImportData(
            meta = ImportMeta(version = "1.0"),
            lists = listOf(importList)
        )
        importUseCase.importLists(importData)

        // Then: No new cards should be created (content already exists)
        verify(mockCardRepository, never()).createCards(any())
    }

    @Test
    fun `syncList only adds cards that do not exist by content`() = runTest {
        // Given: Two new refs, but one has content that already exists
        val existingList = DomainList(id = 1L, title = "Test List", createdAt = Date(), updatedAt = Date())
        val importList = ImportList(
            id = "test-id",
            title = "Test List",
            updatedAt = "2024-01-01T00:00:00Z",
            entries = listOf(
                ImportEntry(ref = 200L, updatedAt = "2024-01-01T00:00:00Z"), // content exists
                ImportEntry(ref = 300L, updatedAt = "2024-01-01T00:00:00Z")  // new content
            )
        )

        whenever(mockListRepository.getListByTitle("Test List")).thenReturn(existingList)
        whenever(mockCardRepository.getExistingRefs(1L)).thenReturn(emptyList()) // no existing refs

        // Entry 200 -> content already exists
        whenever(mockEntryProvider.queryEntry(200L)).thenReturn(
            JapaneseEntry(entry = "日本語", furigana = "にほんご", summary = "Japanese")
        )
        // Entry 300 -> new content
        whenever(mockEntryProvider.queryEntry(300L)).thenReturn(
            JapaneseEntry(entry = "新しい", furigana = "あたらしい", summary = "new")
        )

        // Content check: 日本語 exists, 新しい does not
        whenever(mockCardRepository.cardExists(
            listId = eq(1L),
            japanese = eq("日本語"),
            reading = any(),
            meaning = any(),
            cardType = any()
        )).thenReturn(true)

        whenever(mockCardRepository.cardExists(
            listId = eq(1L),
            japanese = eq("新しい"),
            reading = any(),
            meaning = any(),
            cardType = any()
        )).thenReturn(false)

        // When: We call importLists
        val importData = ImportData(
            meta = ImportMeta(version = "1.0"),
            lists = listOf(importList)
        )
        importUseCase.importLists(importData)

        // Then: Only cards for 新しい should be created
        verify(mockCardRepository).createCards(argThat { cards ->
            cards.isNotEmpty() && cards.all { it.japanese == "新しい" }
        })
    }

    @Test
    fun `importAllEntries creates cards for new list without checking duplicates`() = runTest {
        // Given: A new list that doesn't exist yet
        val importList = ImportList(
            id = "test-id",
            title = "New List",
            updatedAt = "2024-01-01T00:00:00Z",
            entries = listOf(ImportEntry(ref = 100L, updatedAt = "2024-01-01T00:00:00Z"))
        )

        whenever(mockListRepository.getListByTitle("New List")).thenReturn(null) // list doesn't exist
        whenever(mockListRepository.createList("New List")).thenReturn(1L)
        whenever(mockEntryProvider.queryEntry(100L)).thenReturn(
            JapaneseEntry(entry = "日本語", furigana = "にほんご", summary = "Japanese")
        )

        // When: We call importLists
        val importData = ImportData(
            meta = ImportMeta(version = "1.0"),
            lists = listOf(importList)
        )
        importUseCase.importLists(importData)

        // Then: List should be created and cards should be added
        verify(mockListRepository).createList("New List")
        verify(mockCardRepository).createCards(argThat { cards ->
            cards.isNotEmpty() && cards.all { it.japanese == "日本語" }
        })
    }

    @Test
    fun `syncList removes cards that are no longer in the import`() = runTest {
        // Given: A list with an existing card (ref 100), but the import no longer has it
        val existingList = DomainList(id = 1L, title = "Test List", createdAt = Date(), updatedAt = Date())
        val importList = ImportList(
            id = "test-id",
            title = "Test List",
            updatedAt = "2024-01-01T00:00:00Z",
            entries = listOf(ImportEntry(ref = 200L, updatedAt = "2024-01-01T00:00:00Z")) // only ref 200
        )

        whenever(mockListRepository.getListByTitle("Test List")).thenReturn(existingList)
        whenever(mockCardRepository.getExistingRefs(1L)).thenReturn(listOf(100L, 200L)) // has 100 and 200
        whenever(mockEntryProvider.queryEntry(any())).thenReturn(null) // no new entries to add
        whenever(mockCardRepository.cardExists(any(), any(), any(), any(), any())).thenReturn(true)

        // When: We call importLists
        val importData = ImportData(
            meta = ImportMeta(version = "1.0"),
            lists = listOf(importList)
        )
        importUseCase.importLists(importData)

        // Then: Card with ref 100 should be deleted (not in import anymore)
        verify(mockCardRepository).deleteCardsByRefs(eq(1L), argThat { refs ->
            refs.size == 1 && refs.contains(100L)
        })
    }
}
