package com.jflash.domain.model

import java.util.Date

data class List(
    val id: Long,
    val title: String,
    val createdAt: Date,
    val updatedAt: Date
)