package com.example.mindmesh.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mindmesh.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MindMeshNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        NavigationItem("documents", "Documents", Icons.Filled.Description),
        NavigationItem("maps", "Maps", Icons.Filled.AccountTree),
        NavigationItem("flashcards", "Flashcards", Icons.Filled.Quiz),
        NavigationItem("chat", "Chat", Icons.Filled.Chat),
        NavigationItem("settings", "Settings", Icons.Filled.Settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "documents",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("documents") { DocumentsScreen() }
            composable("maps") { CognitiveMapsScreen() }
            composable("flashcards") { FlashcardsScreen() }
            composable("chat") { ChatScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)