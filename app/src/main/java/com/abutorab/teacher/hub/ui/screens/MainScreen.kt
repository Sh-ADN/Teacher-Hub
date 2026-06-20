package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abutorab.teacher.hub.domain.TeacherViewModel

@Composable
fun MainScreen(viewModel: TeacherViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.People, contentDescription = "Students") },
                    label = { Text("Students") },
                    selected = currentRoute == "students",
                    onClick = {
                        navController.navigate("students") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "Subjects") },
                    label = { Text("Subjects") },
                    selected = currentRoute == "subjects",
                    onClick = {
                        navController.navigate("subjects") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Quick Edit") },
                    label = { Text("Quick Edit") },
                    selected = currentRoute == "quick_edit",
                    onClick = {
                        navController.navigate("quick_edit") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Tabulation") },
                    label = { Text("Tabulation") },
                    selected = currentRoute == "tabulation",
                    onClick = {
                        navController.navigate("tabulation") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PersonSearch, contentDescription = "Marksheet") },
                    label = { Text("Marksheet") },
                    selected = currentRoute == "marksheet",
                    onClick = {
                        navController.navigate("marksheet") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "quick_edit",
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            composable("students") { StudentsScreen(viewModel) }
            composable("subjects") { SubjectsScreen(viewModel) }
            composable("quick_edit") { QuickEditScreen(viewModel) }
            composable("tabulation") { TabulationScreen(viewModel) }
            composable("marksheet") { MarksheetScreen(viewModel) }
        }
    }
}
