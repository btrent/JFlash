package com.jflash.data.database

/**
 * Interface for providing Japanese entries from the database.
 * This abstraction allows for easier testing of ImportUseCase.
 */
interface JapaneseEntryProvider {
    fun queryEntry(rowId: Long): JapaneseEntry?
    fun close()
}
