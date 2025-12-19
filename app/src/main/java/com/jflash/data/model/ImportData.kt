package com.jflash.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImportData(
    val meta: ImportMeta,
    val lists: List<ImportList>
)

@Serializable
data class ImportMeta(
    val version: String? = null
)

@Serializable
data class ImportList(
    val id: String,
    val title: String,
    val updatedAt: String,
    val entries: List<ImportEntry>
)

@Serializable
data class ImportEntry(
    val ref: Long,
    val updatedAt: String
)