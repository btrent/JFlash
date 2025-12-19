package com.jflash.domain.model

enum class FSRSGrade(val value: Int) {
    AGAIN(1),    // Forgot
    HARD(2),     // Almost
    GOOD(3),     // Recalled
    EASY(4)      // Easy
}