package com.jflash.data.repository

import com.jflash.data.database.dao.ListDao
import com.jflash.data.database.entity.ListEntity
import com.jflash.domain.model.List as DomainList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListRepository @Inject constructor(
    private val listDao: ListDao
) {
    fun getAllLists(): Flow<List<DomainList>> {
        return listDao.getAllLists().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    suspend fun getListById(id: Long): DomainList? {
        return listDao.getListById(id)?.toDomainModel()
    }

    suspend fun getListById(id: String): DomainList? {
        return listDao.getListById(id.toLong())?.toDomainModel()
    }

    suspend fun getListByTitle(title: String): DomainList? {
        return listDao.getListByTitle(title)?.toDomainModel()
    }

    suspend fun createList(title: String): Long {
        return listDao.insertList(
            ListEntity(title = title)
        )
    }

    suspend fun updateList(list: DomainList) {
        listDao.updateList(list.toEntity())
    }

    suspend fun deleteList(list: DomainList) {
        listDao.deleteList(list.toEntity())
    }

    private fun ListEntity.toDomainModel() = DomainList(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun DomainList.toEntity() = ListEntity(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}