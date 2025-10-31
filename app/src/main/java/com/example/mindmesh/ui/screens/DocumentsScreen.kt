package com.example.mindmesh.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindmesh.data.model.Document
import com.example.mindmesh.viewmodel.DocumentViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: DocumentViewModel = viewModel()
) {
    val context = LocalContext.current
    val documents by viewModel.documents.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    
    var showYouTubeDialog by remember { mutableStateOf(false) }
    var youtubeUrl by remember { mutableStateOf("") }
    
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "Document_${System.currentTimeMillis()}"
            viewModel.uploadDocument(it, fileName)
        }
    }

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
                text = "Documents",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = { showYouTubeDialog = true }) {
                    Icon(Icons.Filled.VideoLibrary, contentDescription = "Add YouTube")
                }
                IconButton(onClick = { documentPicker.launch("*/*") }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Document")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error display
        error?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Documents list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(documents) { document ->
                DocumentCard(
                    document = document,
                    onDelete = { viewModel.deleteDocument(document) }
                )
            }
        }
    }
    
    // YouTube URL Dialog
    if (showYouTubeDialog) {
        AlertDialog(
            onDismissRequest = { showYouTubeDialog = false },
            title = { Text("Add YouTube Video") },
            text = {
                OutlinedTextField(
                    value = youtubeUrl,
                    onValueChange = { youtubeUrl = it },
                    label = { Text("YouTube URL") },
                    placeholder = { Text("https://www.youtube.com/watch?v=...") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (youtubeUrl.isNotEmpty()) {
                            viewModel.addYouTubeVideo(youtubeUrl)
                            youtubeUrl = ""
                            showYouTubeDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showYouTubeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentCard(
    document: Document,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = document.fileType.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (document.isProcessed) {
                        Text(
                            text = "✓ Processed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = document.content.take(100) + if (document.content.length > 100) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}