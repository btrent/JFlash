package com.jflash.ui.screen

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.PaddingValues
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jflash.data.model.CardType
import com.jflash.domain.model.Card
import com.jflash.domain.model.FSRSGrade
import com.jflash.domain.usecase.FSRSAlgorithm
import com.jflash.ui.theme.*
import com.jflash.ui.component.BackgroundImageLayout
import com.jflash.MainActivity
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel = hiltViewModel(),
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentCard by viewModel.currentCard.collectAsStateWithLifecycle()
    val showAnswer by viewModel.showAnswer.collectAsStateWithLifecycle()
    val schedulingInfo by viewModel.schedulingInfo.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val context = LocalContext.current
    
    DisposableEffect(context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setLanguage(Locale.JAPANESE)
            }
        }
        onDispose {
            tts?.shutdown()
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                lists = uiState.lists,
                selectedList = uiState.selectedList,
                onListSelected = { list ->
                    viewModel.selectList(list)
                    scope.launch { drawerState.close() }
                },
                onImportClick = {
                    (context as? MainActivity)?.openFilePicker()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { 
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main content area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentCard != null) {
                        if (!showAnswer) {
                            QuestionView(
                                card = currentCard!!,
                                onTap = { viewModel.showAnswer() }
                            )
                        } else {
                            AnswerView(
                                card = currentCard!!,
                                schedulingInfo = schedulingInfo,
                                onGrade = { grade -> viewModel.gradeCard(grade) },
                                onSpeak = {
                                    tts?.speak(currentCard!!.reading, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                            )
                        }
                    } else {
                        Text(
                            text = "No cards due",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Status bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Reviewed today: ${uiState.reviewedToday}")
                        Text("Due: ${uiState.dueCount}")
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionView(
    card: Card,
    onTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onTap() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (card.cardType) {
                CardType.JP_TO_EN, CardType.JP_TO_READING -> card.japanese
                CardType.EN_TO_JP, CardType.EN_TO_JP_READING -> card.meaning
                CardType.READING_TO_JP_EN -> card.reading
            },
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = if (card.cardType == CardType.EN_TO_JP || card.cardType == CardType.EN_TO_JP_READING) (48 + 30).sp else 48.sp,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
fun AnswerView(
    card: Card,
    schedulingInfo: FSRSAlgorithm.SchedulingInfo?,
    onGrade: (FSRSGrade) -> Unit,
    onSpeak: () -> Unit
) {
    // Determine which text to use for image matching
    val imageMatchText = when {
        card.japanese.isNotBlank() -> card.japanese
        card.reading.isNotBlank() -> card.reading
        else -> card.meaning
    }
    
    BackgroundImageLayout(text = imageMatchText) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Display all information with semi-transparent background
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                color = Color(0x4B808080), // Grey 808080 with 20% opacity (0x33 = 51/255 ≈ 20%)
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = card.japanese,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    
                    if (card.reading != card.japanese) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = card.reading,
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(onClick = onSpeak) {
                                Icon(
                                    Icons.Default.VolumeUp,
                                    contentDescription = "Speak",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = card.meaning,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        
            // Scheduling info with background
            schedulingInfo?.let { info ->
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "See again in ${formatInterval(info.interval)}",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        
            // Grade buttons - flush design like Hanly
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                GradeButton(
                    text = "Forgot",
                    color = ForgotColor,
                    onClick = { onGrade(FSRSGrade.AGAIN) },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        topStart = 12.dp, bottomStart = 12.dp
                    )
                )
                GradeButton(
                    text = "Almost",
                    color = AlmostColor,
                    onClick = { onGrade(FSRSGrade.HARD) },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                )
                GradeButton(
                    text = "Recalled",
                    color = RecalledColor,
                    onClick = { onGrade(FSRSGrade.GOOD) },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
                )
                GradeButton(
                    text = "Easy",
                    color = EasyColor,
                    onClick = { onGrade(FSRSGrade.EASY) },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(
                        topEnd = 12.dp, bottomEnd = 12.dp
                    )
                )
            }
        }
    }
}

@Composable
fun GradeButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.height(48.dp),
        shape = shape,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text, 
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}

@Composable
fun DrawerContent(
    lists: List<com.jflash.domain.model.List>,
    selectedList: com.jflash.domain.model.List?,
    onListSelected: (com.jflash.domain.model.List) -> Unit,
    onImportClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "JFlash",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            
            Divider()
            
            TextButton(
                onClick = onImportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Import list",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }
            
            Divider()
            
            Text(
                text = "Lists",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp)
            )
            
            lists.forEach { list ->
                ListItem(
                    headlineContent = { Text(list.title) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onListSelected(list) }
                        .padding(horizontal = 8.dp),
                    colors = if (list == selectedList) {
                        ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    } else {
                        ListItemDefaults.colors()
                    }
                )
            }
        }
    }
}

private fun formatInterval(days: Int): String {
    return when {
        days == 0 -> "a few minutes"
        days == 1 -> "1 day"
        days < 30 -> "$days days"
        days < 365 -> "${days / 30} months"
        else -> "${days / 365} years"
    }
}