package com.jflash.domain.model

data class Sentence(
    val id: String,
    val volume: String?,
    val kanjiNumber: Int?,
    val sentenceNumber: Int?,
    val japaneseSentence: String,
    val pronunciation: String,
    val englishTranslation: String,
    val notes: String?
)