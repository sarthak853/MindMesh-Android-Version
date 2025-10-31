package com.example.mindmesh.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindmesh.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    chatViewModel: ChatViewModel = viewModel()
) {
    val context = LocalContext.current
    var apiKey by remember { mutableStateOf(chatViewModel.getApiKey()) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // AI Settings Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "AI Chat Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ListItem(
                    headlineContent = { Text("Gemini API Key") },
                    supportingContent = { 
                        Text(if (apiKey.isNotEmpty()) "Configured" else "Not configured") 
                    },
                    leadingContent = {
                        Icon(Icons.Filled.Key, contentDescription = null)
                    },
                    trailingContent = {
                        IconButton(onClick = { showApiKeyDialog = true }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                    }
                )
                
                Divider()
                
                ListItem(
                    headlineContent = { Text("Clear Chat History") },
                    supportingContent = { Text("Remove all chat messages") },
                    leadingContent = {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                    },
                    trailingContent = {
                        IconButton(onClick = { chatViewModel.clearChatHistory() }) {
                            Icon(Icons.Filled.ArrowForward, contentDescription = "Clear")
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "App Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                ListItem(
                    headlineContent = { Text("Version") },
                    supportingContent = { Text("1.0.0") },
                    leadingContent = {
                        Icon(Icons.Filled.Info, contentDescription = null)
                    }
                )
                
                Divider()
                
                ListItem(
                    headlineContent = { Text("About MindMesh") },
                    supportingContent = { Text("AI-powered knowledge management") },
                    leadingContent = {
                        Icon(Icons.Filled.AccountTree, contentDescription = null)
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Features Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Features",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                FeatureItem(
                    title = "Document Processing",
                    description = "PDF, DOCX support with offline text extraction",
                    icon = Icons.Filled.Description
                )
                
                FeatureItem(
                    title = "Cognitive Maps",
                    description = "Interactive knowledge visualization",
                    icon = Icons.Filled.AccountTree
                )
                
                FeatureItem(
                    title = "Flashcards",
                    description = "Spaced repetition learning system",
                    icon = Icons.Filled.Quiz
                )
                
                FeatureItem(
                    title = "AI Chat",
                    description = "Conversational AI powered by Gemini",
                    icon = Icons.Filled.Chat
                )
                
                FeatureItem(
                    title = "Offline First",
                    description = "Core features work without internet",
                    icon = Icons.Filled.CloudOff
                )
            }
        }
    }
    
    // API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Gemini API Key") },
            text = {
                Column {
                    Text("Enter your Gemini API key to enable AI chat features:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        placeholder = { Text("AIza...") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Get your API key from Google AI Studio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        chatViewModel.setApiKey(apiKey)
                        showApiKeyDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FeatureItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    )
}