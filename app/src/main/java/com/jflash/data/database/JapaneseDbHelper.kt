package com.jflash.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

class JapaneseDbHelper(private val context: Context) {
    companion object {
        const val DB_NAME = "Japanese4.db"
        const val ASSETS_DB_PATH = "Japanese4.db"
    }

    private var database: SQLiteDatabase? = null

    fun getDatabase(): SQLiteDatabase {
        if (database == null || !database!!.isOpen) {
            val dbFile = getDatabaseFile()
            if (!dbFile.exists()) {
                copyDatabaseFromAssets()
            }
            database = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
        }
        return database!!
    }

    fun queryEntry(rowId: Long): JapaneseEntry? {
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

    private fun getDatabaseFile(): File {
        return File(context.filesDir, DB_NAME)
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

    fun close() {
        database?.close()
        database = null
    }
}

data class JapaneseEntry(
    val entry: String,
    val furigana: String?,
    val summary: String?
)