package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.data.SubjectEntity
import com.abutorab.teacher.hub.domain.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditScreen(viewModel: TeacherViewModel) {
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val data by viewModel.activeQuickEditData.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Quick Edit Marks",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Subject Selector
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedSubject?.title ?: "Select Subject",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                label = { Text("Select Subject") }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                allSubjects.forEach { subj ->
                    DropdownMenuItem(
                        text = { Text(subj.title) },
                        onClick = {
                            viewModel.selectSubject(subj.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Students List
        if (selectedSubject != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().imePadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(data, key = { it.student.rollNumber }) { item ->
                    StudentMarkRow(
                        item = item,
                        subject = selectedSubject!!,
                        onMarkChanged = { mcq, written, pract ->
                            viewModel.saveMark(item.student.rollNumber, mcq, written, pract)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentMarkRow(
    item: com.abutorab.teacher.hub.domain.StudentWithMark,
    subject: SubjectEntity,
    onMarkChanged: (Int?, Int?, Int?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Roll: ${item.student.rollNumber} - ${item.student.name}", 
                style = MaterialTheme.typography.titleMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp), 
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var mcqText by remember(item.mark?.mcq) { mutableStateOf(item.mark?.mcq?.toString() ?: "") }
                var writtenText by remember(item.mark?.written) { mutableStateOf(item.mark?.written?.toString() ?: "") }
                var practicalText by remember(item.mark?.practical) { mutableStateOf(item.mark?.practical?.toString() ?: "") }

                if (subject.hasMcq) {
                    OutlinedTextField(
                        value = mcqText,
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (newVal.isEmpty() || (parsed != null && parsed <= subject.maxMcq)) {
                                mcqText = newVal
                                onMarkChanged(newVal.toIntOrNull(), item.mark?.written, item.mark?.practical)
                            }
                        },
                        label = { Text("MCQ (Max ${subject.maxMcq})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = (mcqText.toIntOrNull() ?: 0) > subject.maxMcq
                    )
                }
                
                if (subject.hasWritten) {
                    OutlinedTextField(
                        value = writtenText,
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (newVal.isEmpty() || (parsed != null && parsed <= subject.maxWritten)) {
                                writtenText = newVal
                                onMarkChanged(item.mark?.mcq, newVal.toIntOrNull(), item.mark?.practical)
                            }
                        },
                        label = { Text("Written (Max ${subject.maxWritten})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = (writtenText.toIntOrNull() ?: 0) > subject.maxWritten
                    )
                }
                
                if (subject.hasPractical) {
                    OutlinedTextField(
                        value = practicalText,
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (newVal.isEmpty() || (parsed != null && parsed <= subject.maxPractical)) {
                                practicalText = newVal
                                onMarkChanged(item.mark?.mcq, item.mark?.written, newVal.toIntOrNull())
                            }
                        },
                        label = { Text("Practical (Max ${subject.maxPractical})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = (practicalText.toIntOrNull() ?: 0) > subject.maxPractical
                    )
                }
            }
            if (item.mark?.mcq == null && item.mark?.written == null && item.mark?.practical == null) {
                Text(
                    text = "No marks entered",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
