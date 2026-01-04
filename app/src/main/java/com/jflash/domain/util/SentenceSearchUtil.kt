package com.jflash.domain.util

object SentenceSearchUtil {
    
    fun extractSearchTerm(japanese: String): String {
        if (japanese.isEmpty()) return japanese
        
        // Check if the string contains any kanji
        val hasKanji = japanese.any { isKanji(it) }
        
        if (!hasKanji) {
            // Pure hiragana or katakana - return as is
            return japanese
        }
        
        // Remove trailing hiragana only
        var endIndex = japanese.length
        while (endIndex > 0 && isHiragana(japanese[endIndex - 1])) {
            endIndex--
        }
        
        return if (endIndex > 0) japanese.substring(0, endIndex) else japanese
    }
    
    private fun isKanji(char: Char): Boolean {
        return char in '\u4e00'..'\u9faf'
    }
    
    private fun isHiragana(char: Char): Boolean {
        return char in '\u3041'..'\u3096'
    }
    
    private fun isKatakana(char: Char): Boolean {
        return char in '\u30a1'..'\u30f6'
    }
}