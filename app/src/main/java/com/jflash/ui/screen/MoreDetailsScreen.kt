package com.jflash.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jflash.domain.model.Sentence

@Composable
fun MoreDetailsScreen(
    sentences: List<Sentence>,
    modifier: Modifier = Modifier
) {
    val sepiaBackground = Color(red = 237, green = 223, blue = 201)
    val darkText = Color(red = 97, green = 78, blue = 55)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(sepiaBackground)
            .padding(16.dp)
    ) {
        if (sentences.isEmpty()) {
            Text(
                text = "No example sentences found",
                color = darkText,
                fontSize = 18.sp,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sentences) { sentence ->
                    SentenceItem(sentence = sentence, textColor = darkText)
                }
            }
        }
    }
}

@Composable
private fun SentenceItem(
    sentence: Sentence,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Japanese Sentence
        Text(
            text = sentence.japaneseSentence,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp
        )
        
        // Pronunciation
        Text(
            text = sentence.pronunciation,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            lineHeight = 20.sp
        )
        
        // English Translation
        Text(
            text = sentence.englishTranslation,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp
        )
    }
}