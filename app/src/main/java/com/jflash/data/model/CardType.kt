package com.jflash.data.model

enum class CardType(val description: String) {
    JP_TO_EN("Japanese → English"),
    EN_TO_JP("English → Japanese"),
    JP_TO_READING("Japanese → Reading"),
    READING_TO_JP_EN("Reading → Japanese + English"),
    EN_TO_JP_READING("English → Japanese + Reading")
}