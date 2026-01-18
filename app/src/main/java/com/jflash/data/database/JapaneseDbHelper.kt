package com.jflash.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.jflash.domain.model.Sentence
import java.io.File
import java.io.FileOutputStream

class JapaneseDbHelper(private val context: Context) : JapaneseEntryProvider {
    companion object {
        const val DB_NAME = "Japanese4.db"
        const val ASSETS_DB_PATH = "Japanese4.db"
        const val DB_VERSION = 3 // Increment this when database changes
    }

    private var database: SQLiteDatabase? = null

    fun getDatabase(): SQLiteDatabase {
        if (database == null || !database!!.isOpen) {
            val dbFile = getDatabaseFile()
            val versionFile = getVersionFile()
            
            // Check if database needs to be updated
            val needsUpdate = !dbFile.exists() || 
                !versionFile.exists() || 
                versionFile.readText().toIntOrNull() != DB_VERSION
            
            if (needsUpdate) {
                copyDatabaseFromAssets()
                versionFile.writeText(DB_VERSION.toString())
            }
            
            database = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        }
        return database!!
    }

    override fun queryEntry(rowId: Long): JapaneseEntry? {
        val cursor = getDatabase().query(
            "entries",
            arrayOf("Entry", "Furigana", "Summary"),
            "ROWID = ?",
            arrayOf(rowId.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                JapaneseEntry(
                    entry = it.getString(0) ?: "",
                    furigana = it.getString(1),
                    summary = it.getString(2)
                )
            } else {
                null
            }
        }
    }

    fun searchSentences(searchTerm: String): List<Sentence> {
        val sentences = mutableListOf<Sentence>()
        
        try {
            val cursor = getDatabase().query(
                "klc_sentences",
                arrayOf("ID", "VOLUME", "KANJI_NUMBER", "SENTENCE_NUMBER", "JAPANESE_SENTENCE", "PRONUNCIATION", "ENGLISH_TRANSLATION", "NOTES"),
                "JAPANESE_SENTENCE LIKE ?",
                arrayOf("%$searchTerm%"),
                null,
                null,
                null
            )

            cursor.use {
                while (it.moveToNext()) {
                    sentences.add(
                        Sentence(
                            id = it.getString(0) ?: "",
                            volume = it.getString(1),
                            kanjiNumber = if (it.isNull(2)) null else it.getInt(2),
                            sentenceNumber = if (it.isNull(3)) null else it.getInt(3),
                            japaneseSentence = it.getString(4) ?: "",
                            pronunciation = it.getString(5) ?: "",
                            englishTranslation = it.getString(6) ?: "",
                            notes = it.getString(7)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // If the table doesn't exist or there's an error, return empty list
            // This handles cases where the database hasn't been updated yet
            android.util.Log.w("JapaneseDbHelper", "Failed to search sentences: ${e.message}")
        }
        
        return sentences
    }

    private fun getDatabaseFile(): File {
        return File(context.filesDir, DB_NAME)
    }
    
    private fun getVersionFile(): File {
        return File(context.filesDir, "${DB_NAME}.version")
    }

    private fun copyDatabaseFromAssets() {
        val dbFile = getDatabaseFile()
        dbFile.parentFile?.mkdirs()

        context.assets.open(ASSETS_DB_PATH).use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    override fun close() {
        database?.close()
        database = null
    }
}

data class JapaneseEntry(
    val entry: String,
    val furigana: String?,
    val summary: String?
)