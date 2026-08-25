package com.abutorab.teacher.hub.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.R
import com.abutorab.teacher.hub.domain.TeacherViewModel
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    QUICK_EDIT("Quick Mark Entry"),
    STUDENTS("Students Roster"),
    SUBJECTS("Subjects & Curriculum"),
    TABULATION("Tabulation Sheet"),
    MARKSHEET("Individual Marksheet"),
    MERIT_LIST("Merit Ranking List"),
    YEAR_PICKER("Academic Year"),
    TERM_PICKER("Academic Term"),
    SETTINGS("Settings & Setup")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: TeacherViewModel) {
    val backStack = remember { mutableStateListOf(AppScreen.TABULATION) }
    val currentScreen = backStack.last()

    fun navigateTo(screen: AppScreen) {
        if (backStack.contains(screen)) {
            backStack.remove(screen)
        }
        backStack.add(screen)
    }

    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedTerm by viewModel.selectedTerm.collectAsStateWithLifecycle()

    LaunchedEffect(selectedTerm, currentScreen) {
        if (selectedTerm == "SOMONNITO" && currentScreen == AppScreen.QUICK_EDIT) {
            navigateTo(AppScreen.TABULATION)
        }
    }
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val teacherName by viewModel.teacherName.collectAsStateWithLifecycle()
    val schoolName by viewModel.schoolName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()

    val termDisplay = when (selectedTerm) {
        "ARDHOBARSHIK" -> "অর্ধবার্ষিক"
        "BARSHIK" -> "বার্ষিক"
        "SOMONNITO" -> "সমন্বিত"
        else -> selectedTerm
    }

    val animatedSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.surfaceVariant, animationSpec = tween(300))
    val animatedOnSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurfaceVariant, animationSpec = tween(300))
    val animatedPrimary by animateColorAsState(targetValue = MaterialTheme.colorScheme.primary, animationSpec = tween(300))

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(320.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header with School Logo, School Name, and Teacher Profile
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.school_logo),
                                    contentDescription = "School Logo",
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(4.dp)
                                )
                                Column {
                                    Text(
                                        text = schoolName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Teacher Hub • Academic Suite",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            if (isLoggedIn) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                                            contentDescription = null,
                                            tint = if (isLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (isLoggedIn) teacherName else "Google Account",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = if (isLoggedIn) userEmail else "Not signed in",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Section 1: Mark Management & Records
                    Text(
                        "MARKS & ACADEMIC RECORDS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    if (selectedTerm != "SOMONNITO") {
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            label = { Text("Quick Mark Entry") },
                            selected = currentScreen == AppScreen.QUICK_EDIT,
                            onClick = {
                                navigateTo(AppScreen.QUICK_EDIT)
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Students Roster") },
                        selected = currentScreen == AppScreen.STUDENTS,
                        onClick = {
                            navigateTo(AppScreen.STUDENTS)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.School, contentDescription = null) },
                        label = { Text("Subjects & Curriculum") },
                        selected = currentScreen == AppScreen.SUBJECTS,
                        onClick = {
                            navigateTo(AppScreen.SUBJECTS)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // Section 2: Main Overview
                    Text(
                        "MAIN OVERVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                        label = { Text("Dashboard & Analytics") },
                        selected = currentScreen == AppScreen.DASHBOARD,
                        onClick = {
                            navigateTo(AppScreen.DASHBOARD)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                        label = { Text("Tabulation Sheet") },
                        selected = currentScreen == AppScreen.TABULATION,
                        onClick = {
                            navigateTo(AppScreen.TABULATION)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Badge, contentDescription = null) },
                        label = { Text("Individual Marksheet") },
                        selected = currentScreen == AppScreen.MARKSHEET,
                        onClick = {
                            navigateTo(AppScreen.MARKSHEET)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                        label = { Text("Merit Ranking List") },
                        selected = currentScreen == AppScreen.MERIT_LIST,
                        onClick = {
                            navigateTo(AppScreen.MERIT_LIST)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // Section 3: Session
                    Text(
                        "ACADEMIC SESSION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        label = { Text("Academic Year ($selectedYear)") },
                        selected = currentScreen == AppScreen.YEAR_PICKER,
                        onClick = {
                            navigateTo(AppScreen.YEAR_PICKER)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.EventNote, contentDescription = null) },
                        label = { Text("Academic Term ($termDisplay)") },
                        selected = currentScreen == AppScreen.TERM_PICKER,
                        onClick = {
                            navigateTo(AppScreen.TERM_PICKER)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // Section 4: System & Setup
                    Text(
                        "SYSTEM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings & Setup") },
                        selected = currentScreen == AppScreen.SETTINGS,
                        onClick = {
                            navigateTo(AppScreen.SETTINGS)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentScreen.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$selectedYear • $termDisplay",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Hamburger Menu")
                        }
                    },
                    actions = {
                        // Quick Theme Toggle
                        IconButton(onClick = {
                            val nextMode = when (themeMode) {
                                "dark" -> "light"
                                "light" -> "dark"
                                else -> "dark"
                            }
                            viewModel.setThemeMode(nextMode)
                        }) {
                            Icon(
                                if (themeMode == "dark") Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle Light/Dark Theme"
                            )
                        }

                        // Quick Term Selector Chip
                        AssistChip(
                            onClick = { navigateTo(AppScreen.TERM_PICKER) },
                            label = { Text(termDisplay, fontSize = 11.sp) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (currentScreen in listOf(AppScreen.TABULATION, AppScreen.MARKSHEET)) {
                    NavigationBar(containerColor = animatedSurfaceVariant) {
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.TABULATION,
                            onClick = { navigateTo(AppScreen.TABULATION) },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Tabulation") },
                            label = { Text("Tabulation") }
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.MARKSHEET,
                            onClick = { navigateTo(AppScreen.MARKSHEET) },
                            icon = { Icon(Icons.Default.Person, contentDescription = "Marksheet") },
                            label = { Text("Marksheet") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Surface(modifier = Modifier.padding(innerPadding)) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToMeritList = { navigateTo(AppScreen.MERIT_LIST) }
                    )
                    AppScreen.QUICK_EDIT -> QuickEditScreen(viewModel = viewModel)
                    AppScreen.STUDENTS -> StudentsScreen(viewModel = viewModel)
                    AppScreen.SUBJECTS -> SubjectsScreen(viewModel = viewModel)
                    AppScreen.TABULATION -> TabulationScreen(
                        viewModel = viewModel,
                        onNavigateToMarksheet = { rollNumber -> 
                            viewModel.onMarksheetSearchChanged(rollNumber.toString())
                            navigateTo(AppScreen.MARKSHEET) 
                        }
                    )
                    AppScreen.MARKSHEET -> MarksheetScreen(viewModel = viewModel)
                    AppScreen.MERIT_LIST -> MeritListScreen(
                        viewModel = viewModel,
                        onBack = { if (backStack.size > 1) backStack.removeAt(backStack.lastIndex) }
                    )
                    AppScreen.YEAR_PICKER -> YearPickerScreen(
                        onYearSelected = { year ->
                            viewModel.selectYear(year.toString())
                            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                        }
                    )
                    AppScreen.TERM_PICKER -> TermPickerScreen(
                        selectedYear = selectedYear.toIntOrNull() ?: 2026,
                        onTermSelected = { term ->
                            viewModel.selectTerm(term)
                            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                        },
                        onBack = { 
                            if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
                        }
                    )
                    AppScreen.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToYearPicker = { navigateTo(AppScreen.YEAR_PICKER) },
                        onNavigateToTermPicker = { navigateTo(AppScreen.TERM_PICKER) }
                    )
                }
            }
        }
    }
}
