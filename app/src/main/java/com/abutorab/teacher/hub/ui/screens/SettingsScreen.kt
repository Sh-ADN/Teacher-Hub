package com.abutorab.teacher.hub.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.domain.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TeacherViewModel,
    onNavigateToYearPicker: (() -> Unit)? = null,
    onNavigateToTermPicker: (() -> Unit)? = null
) {
    val selectedTerm by viewModel.selectedTerm.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val geminiApiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val teacherName by viewModel.teacherName.collectAsStateWithLifecycle()
    val schoolName by viewModel.schoolName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    
    val uriHandler = LocalUriHandler.current
    val context = androidx.compose.ui.platform.LocalContext.current

    var showProfileDialog by remember { mutableStateOf(false) }
    var showResetSubjectsDialog by remember { mutableStateOf(false) }

    val animatedSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.surfaceVariant, animationSpec = tween(300))
    val animatedOnSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurfaceVariant, animationSpec = tween(300))
    val animatedOnSurface by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurface, animationSpec = tween(300))
    val animatedSurface by animateColorAsState(targetValue = MaterialTheme.colorScheme.surface, animationSpec = tween(300))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(animatedSurfaceVariant)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Settings & Setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = animatedOnSurfaceVariant
        )

        // 1. Google Sign-In & Teacher Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = animatedSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            modifier = Modifier.size(44.dp).clip(CircleShape),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isLoggedIn) Icons.Default.AccountCircle else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                if (isLoggedIn) teacherName else "Teacher Account",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = animatedOnSurface
                            )
                            Text(
                                if (isLoggedIn) "$userEmail • $schoolName" else "Sign in to sync marks & reports",
                                style = MaterialTheme.typography.bodySmall,
                                color = animatedOnSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider()

                if (isLoggedIn) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { showProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Profile")
                        }
                        TextButton(
                            onClick = { viewModel.signOut(context) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Sign Out")
                        }
                    }
                } else {
                    
                    Button(
                        onClick = { viewModel.signInWithGoogle(context) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign In with Google")
                    }
                }
            }
        }

        // 2. Theme & Display (Light / Dark Mode Toggle)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = animatedSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Appearance & Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = animatedOnSurface
                )
                Text(
                    "Choose between Light, Dark, or System theme mode",
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedOnSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = themeMode == "system",
                        onClick = { viewModel.setThemeMode("system") },
                        leadingIcon = { Icon(Icons.Default.BrightnessAuto, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("System") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeMode == "light",
                        onClick = { viewModel.setThemeMode("light") },
                        leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Light") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = themeMode == "dark",
                        onClick = { viewModel.setThemeMode("dark") },
                        leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        label = { Text("Dark") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Academic Session (Term & Year)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = animatedSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Academic Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = animatedOnSurface
                )

                Text("Academic Term", style = MaterialTheme.typography.labelMedium, color = animatedOnSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val terms = listOf(
                        "ARDHOBARSHIK" to "অর্ধবার্ষিক",
                        "BARSHIK" to "বার্ষিক",
                        "SOMONNITO" to "সমন্বিত"
                    )
                    terms.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedTerm == key,
                            onClick = { viewModel.selectTerm(key) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Academic Year", style = MaterialTheme.typography.titleSmall, color = animatedOnSurface)
                        Text(selectedYear, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    if (onNavigateToYearPicker != null) {
                        OutlinedButton(onClick = onNavigateToYearPicker) {
                            Text("Change Year")
                        }
                    }
                }
            }
        }

        // 4. Fixed Subjects & Curriculum Setup
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = animatedSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Curriculum & Fixed Subjects",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = animatedOnSurface
                        )
                        Text(
                            "Restore standard NCTB secondary subjects (Bangla, English, Math, Science, ICT, Agriculture)",
                            style = MaterialTheme.typography.bodySmall,
                            color = animatedOnSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showResetSubjectsDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Load Fixed NCTB Subjects")
                }
            }
        }

        // 5. AI Scanner Configuration
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = animatedSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "AI Marksheet Scanner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = animatedOnSurface
                )

                var apiKeyInput by remember(geminiApiKey) { mutableStateOf(geminiApiKey) }
                var isEditingKey by remember { mutableStateOf(false) }

                if (isEditingKey) {
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { isEditingKey = false; apiKeyInput = geminiApiKey }) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            viewModel.saveGeminiApiKey(apiKeyInput.trim())
                            isEditingKey = false
                        }) {
                            Text("Save")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Gemini API Key", style = MaterialTheme.typography.bodyLarge, color = animatedOnSurface)
                            if (geminiApiKey.isEmpty()) {
                                Text("Not configured (Tap to configure)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("••••••••••••••••", style = MaterialTheme.typography.bodySmall, color = animatedOnSurfaceVariant)
                            }
                        }
                        Button(onClick = { isEditingKey = true }) {
                            Text(if (geminiApiKey.isEmpty()) "Set Key" else "Update")
                        }
                    }
                    Text(
                        "Enables instant optical character recognition for paper marksheets and handwritten score sheets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = animatedOnSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    
                    TextButton(
                        onClick = {
                            try {
                                uriHandler.openUri("https://aistudio.google.com/app/apikey")
                            } catch (e: Exception) {
                                android.widget.Toast.makeText(context, "No web browser found to open link.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Get a free Gemini API Key")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }

    // Google Sign-in Dialog

    // Edit Profile Dialog
    if (showProfileDialog) {
        var nameInput by remember(teacherName) { mutableStateOf(teacherName) }
        var schoolInput by remember(schoolName) { mutableStateOf(schoolName) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("Teacher Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Teacher Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = schoolInput,
                        onValueChange = { schoolInput = it },
                        label = { Text("School Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateTeacherProfile(nameInput.trim(), schoolInput.trim())
                    showProfileDialog = false
                }) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore subjects dialog
    if (showResetSubjectsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSubjectsDialog = false },
            title = { Text("Load Standard NCTB Subjects?") },
            text = { Text("This will configure Bangladesh National Curriculum standard subjects (Bangla 1st/2nd, English 1st/2nd, Math, Religion, BGS, Science, ICT, Agriculture) with default mark distribution.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetAllSubjectsToDefault()
                    showResetSubjectsDialog = false
                }) {
                    Text("Load Subjects")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSubjectsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
