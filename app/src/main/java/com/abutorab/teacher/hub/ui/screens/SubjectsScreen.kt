package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.data.SubjectEntity
import com.abutorab.teacher.hub.domain.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(viewModel: TeacherViewModel) {
    val allSubjectsRaw by viewModel.allSubjects.collectAsStateWithLifecycle()
    val allSubjects = remember(allSubjectsRaw) {
        allSubjectsRaw.sortedBy { subj ->
            val idx = com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.indexOfFirst { it.id == subj.id }
            if (idx == -1) Int.MAX_VALUE else idx
        }
    }
    
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var subjectToDelete by remember { mutableStateOf<SubjectEntity?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Manage Subjects",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(allSubjects, key = { it.id }) { subject ->
                val isCustom = com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.none { it.id == subject.id }
                val bengaliTitle = com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.find { it.id == subject.id }?.bengaliTitle ?: subject.title
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MenuBook,
                                contentDescription = "Subject",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$bengaliTitle [ID: ${subject.id}]",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Row {
                                    IconButton(onClick = { editingSubject = subject }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    if (isCustom) {
                                        IconButton(onClick = { subjectToDelete = subject }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                                        }
                                    }
                                }
                            }
                            Text("Total Max: ${subject.maxMarks} | Pass: ${subject.passMarks}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "MCQ: ${if (subject.hasMcq) subject.maxMcq else "No"} | " +
                                       "Written: ${if (subject.hasWritten) subject.maxWritten else "No"} | " +
                                       "Practical: ${if (subject.hasPractical) subject.maxPractical else "No"}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (subjectToDelete != null) {
        val st = subjectToDelete!!
        var replaceWithId by remember { mutableStateOf<String?>(null) }
        var expanded by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = { Text("Delete Subject") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("You can just delete this subject or replace it with an existing one. If replaced, all marks for this subject will be transferred to the new one.")
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = replaceWithId?.let { id -> 
                                allSubjects.find { it.id == id }?.let { subj ->
                                    com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.find { p -> p.id == subj.id }?.bengaliTitle ?: subj.title
                                }
                            } ?: "Select replacement (optional)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Replace with") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            allSubjects.filter { it.id != st.id }.forEach { subj ->
                                val bTitle = com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.find { p -> p.id == subj.id }?.bengaliTitle ?: subj.title
                                DropdownMenuItem(
                                    text = { Text(bTitle) },
                                    onClick = {
                                        replaceWithId = subj.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSubject(st, replaceWithId)
                        subjectToDelete = null
                    }
                ) {
                    Text(if (replaceWithId != null) "Replace & Delete" else "Just Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (editingSubject != null) {
        val st = editingSubject
        var id by remember { mutableStateOf(st?.id ?: "") }
        var title by remember { mutableStateOf(st?.title ?: "") }
        var maxMarks by remember { mutableStateOf(st?.maxMarks?.toString() ?: "100") }
        var passMarks by remember { mutableStateOf(st?.passMarks?.toString() ?: "33") }
        
        var hasMcq by remember { mutableStateOf(st?.hasMcq ?: true) }
        var maxMcq by remember { mutableStateOf(st?.maxMcq?.toString() ?: "30") }
        
        var hasWritten by remember { mutableStateOf(st?.hasWritten ?: true) }
        var maxWritten by remember { mutableStateOf(st?.maxWritten?.toString() ?: "70") }

        var hasPractical by remember { mutableStateOf(st?.hasPractical ?: true) }
        var maxPractical by remember { mutableStateOf(st?.maxPractical?.toString() ?: "0") }

        AlertDialog(
            onDismissRequest = { editingSubject = null },
            title = { Text("Edit Subject") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = {},
                        label = { Text("Subject") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = maxMarks, onValueChange = { maxMarks = it }, label = { Text("Total Max Marks") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = passMarks, onValueChange = { passMarks = it }, label = { Text("Pass Marks") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Components", style = MaterialTheme.typography.titleSmall)
                    
                    // MCQ
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasMcq, onCheckedChange = { hasMcq = it })
                        Text("MCQ")
                        if (hasMcq) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = maxMcq, onValueChange = { maxMcq = it }, label = { Text("Max") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().weight(1f), singleLine = true)
                        }
                    }
                    
                    // Written
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasWritten, onCheckedChange = { hasWritten = it })
                        Text("Written")
                        if (hasWritten) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = maxWritten, onValueChange = { maxWritten = it }, label = { Text("Max") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().weight(1f), singleLine = true)
                        }
                    }
                    
                    // Practical
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasPractical, onCheckedChange = { hasPractical = it })
                        Text("Practical")
                        if (hasPractical) {
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(value = maxPractical, onValueChange = { maxPractical = it }, label = { Text("Max") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth().weight(1f), singleLine = true)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsedMax = maxMarks.toIntOrNull() ?: 100
                        val parsedPass = passMarks.toIntOrNull() ?: 33
                        val pmcq = maxMcq.toIntOrNull() ?: 0
                        val pwritten = maxWritten.toIntOrNull() ?: 0
                        val ppractical = maxPractical.toIntOrNull() ?: 0
                        
                        if (id.isNotBlank() && title.isNotBlank()) {
                            val newSubject = SubjectEntity(id, title, parsedMax, parsedPass, hasMcq, pmcq, hasWritten, pwritten, hasPractical, ppractical)
                            viewModel.updateSubject(newSubject)
                            editingSubject = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    editingSubject = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
