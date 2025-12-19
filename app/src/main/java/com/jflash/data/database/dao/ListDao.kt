package com.jflash.data.database.dao

import androidx.room.*
import com.jflash.data.database.entity.ListEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ListDao {
    @Query("SELECT * FROM lists ORDER BY title")
    fun getAllLists(): Flow<List<ListEntity>>

    @Query("SELECT * FROM lists WHERE id = :id")
    suspend fun getListById(id: Long): ListEntity?

    @Query("SELECT * FROM lists WHERE title = :title")
    suspend fun getListByTitle(title: String): ListEntity?

    @Insert
    suspend fun insertList(list: ListEntity): Long

    @Update
    suspend fun updateList(list: ListEntity)

    @Delete
    suspend fun deleteList(list: ListEntity)

    @Query("UPDATE lists SET updatedAt = :date WHERE id = :listId")
    suspend fun updateListTimestamp(listId: Long, date: Date = Date())
}