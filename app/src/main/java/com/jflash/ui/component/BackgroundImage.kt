package com.jflash.ui.component

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.random.Random

@Composable
fun BackgroundImageLayout(
    text: String,
    content: @Composable BoxScope.() -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    
    var backgroundImagePath by remember { mutableStateOf<String?>(null) }
    var averageColor by remember { mutableStateOf(Color.Black) }
    var imageHeight by remember { mutableStateOf(0f) }
    
    LaunchedEffect(text) {
        val imagePath = findMatchingImage(context, text)
        backgroundImagePath = imagePath
        
        imagePath?.let { path ->
            val color = calculateAverageColor(context, path)
            averageColor = color
            
            // Calculate scaled height
            val originalHeight = getImageHeight(context, path)
            val scaleFactor = screenWidthPx / 1024f
            imageHeight = originalHeight * scaleFactor
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        backgroundImagePath?.let { imagePath ->
            BackgroundImageWithPadding(
                imagePath = imagePath,
                screenHeight = screenHeightPx,
                imageHeight = imageHeight,
                averageColor = averageColor
            )
        }
        
        content()
    }
}

@Composable
private fun BackgroundImageWithPadding(
    imagePath: String,
    screenHeight: Float,
    imageHeight: Float,
    averageColor: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        val topPadding = maxOf(0f, (screenHeight - imageHeight) / 2f)
        val bottomPadding = maxOf(0f, (screenHeight - imageHeight) / 2f)
        
        // Top padding bar
        if (topPadding > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) { topPadding.toDp() })
                    .background(averageColor)
            )
        }
        
        // Image
        AsyncImage(
            model = "file:///android_asset/bg/$imagePath",
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { 
                    minOf(imageHeight, screenHeight).toDp() 
                }),
            contentScale = ContentScale.Crop
        )
        
        // Bottom padding bar
        if (bottomPadding > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(averageColor)
            )
        }
    }
}

private suspend fun findMatchingImage(context: Context, text: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val assetManager = context.assets
            val bgFiles = assetManager.list("bg") ?: return@withContext null
            
            // Look for exact match (without extension)
            val exactMatch = bgFiles.find { fileName ->
                val nameWithoutExt = fileName.substringBeforeLast(".")
                nameWithoutExt == text
            }
            
            if (exactMatch != null) {
                return@withContext exactMatch
            }
            
            // Check if any character in the text matches a filename
            text.forEach { char ->
                val charMatch = bgFiles.find { fileName ->
                    val nameWithoutExt = fileName.substringBeforeLast(".")
                    nameWithoutExt.startsWith(char.toString())
                }
                if (charMatch != null) {
                    return@withContext charMatch
                }
            }
            
            // Return random image if no match
            if (bgFiles.isNotEmpty()) {
                return@withContext bgFiles[Random.nextInt(bgFiles.size)]
            }
            
            null
        } catch (e: IOException) {
            null
        }
    }
}

private suspend fun calculateAverageColor(context: Context, imagePath: String): Color {
    return withContext(Dispatchers.IO) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("bg/$imagePath")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) return@withContext Color.Black
            
            // Sample pixels from the image for average color calculation
            val sampleSize = 50
            val stepX = maxOf(1, bitmap.width / sampleSize)
            val stepY = maxOf(1, bitmap.height / sampleSize)
            
            var totalRed = 0L
            var totalGreen = 0L
            var totalBlue = 0L
            var sampleCount = 0L
            
            for (x in 0 until bitmap.width step stepX) {
                for (y in 0 until bitmap.height step stepY) {
                    val pixel = bitmap.getPixel(x, y)
                    totalRed += pixel.red
                    totalGreen += pixel.green
                    totalBlue += pixel.blue
                    sampleCount++
                }
            }
            
            bitmap.recycle()
            
            if (sampleCount > 0) {
                val avgRed = (totalRed / sampleCount).toInt()
                val avgGreen = (totalGreen / sampleCount).toInt()
                val avgBlue = (totalBlue / sampleCount).toInt()
                Color(avgRed, avgGreen, avgBlue)
            } else {
                Color.Black
            }
        } catch (e: IOException) {
            Color.Black
        }
    }
}

private suspend fun getImageHeight(context: Context, imagePath: String): Float {
    return withContext(Dispatchers.IO) {
        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("bg/$imagePath")
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            
            options.outHeight.toFloat()
        } catch (e: IOException) {
            1782f // Default height
        }
    }
}