package com.example.mindmesh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindmesh.data.model.ReviewResult
import com.example.mindmesh.viewmodel.FlashcardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    viewModel: FlashcardViewModel = viewModel()
) {
    val currentFlashcard by viewModel.currentFlashcard.observeAsState()
    val showAnswer by viewModel.showAnswer.observeAsState(false)
    val dueCount by viewModel.dueCount.observeAsState(0)
    val isLoading by viewModel.isLoading.observeAsState(false)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Flashcards",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { viewModel.refreshDueFlashcards() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Due count
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = "$dueCount cards due for review",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (currentFlashcard != null) {
            // Flashcard display
            FlashcardView(
                flashcard = currentFlashcard!!,
                showAnswer = showAnswer,
                onShowAnswer = { viewModel.showAnswer() },
                onReview = { result -> viewModel.reviewFlashcard(result) },
                progress = viewModel.getProgress()
            )
        } else {
            // No cards available
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "All caught up!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "No flashcards due for review right now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FlashcardView(
    flashcard: com.example.mindmesh.data.model.Flashcard,
    showAnswer: Boolean,
    onShowAnswer: () -> Unit,
    onReview: (ReviewResult) -> Unit,
    progress: Pair<Int, Int>
) {
    Column {
        // Progress indicator
        Text(
            text = "${progress.first} / ${progress.second}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        LinearProgressIndicator(
            progress = progress.first.toFloat() / progress.second.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Flashcard content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (showAnswer) "Answer:" else "Question:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (showAnswer) flashcard.back else flashcard.front,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!showAnswer) {
            // Show answer button
            Button(
                onClick = onShowAnswer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Answer")
            }
        } else {
            // Review buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "How well did you know this?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onReview(ReviewResult.AGAIN) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Again")
                    }
                    
                    Button(
                        onClick = { onReview(ReviewResult.HARD) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Hard")
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onReview(ReviewResult.GOOD) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Good")
                    }
                    
                    Button(
                        onClick = { onReview(ReviewResult.EASY) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Easy")
                    }
                }
            }
        }
    }
}