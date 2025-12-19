package com.jflash

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.jflash.domain.usecase.ImportUseCase
import com.jflash.ui.screen.ReviewScreen
import com.jflash.ui.theme.JFlashTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var importUseCase: ImportUseCase
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleImport(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle share intent
        if (intent?.action == Intent.ACTION_SEND) {
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                handleImport(uri)
            }
        }
        
        setContent {
            JFlashTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ReviewScreen(
                        onMenuClick = { /* TODO */ }
                    )
                }
            }
        }
    }
    
    private fun handleImport(uri: Uri) {
        lifecycleScope.launch {
            when (val result = importUseCase.importFromUri(uri)) {
                is ImportUseCase.ImportResult.Success -> {
                    Toast.makeText(this@MainActivity, "Import successful", Toast.LENGTH_SHORT).show()
                }
                is ImportUseCase.ImportResult.Error -> {
                    Toast.makeText(this@MainActivity, "Import failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    fun openFilePicker() {
        filePickerLauncher.launch("*/*")
    }
}