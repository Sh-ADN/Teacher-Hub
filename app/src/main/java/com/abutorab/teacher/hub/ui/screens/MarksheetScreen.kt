package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.data.SubjectEntity
import com.abutorab.teacher.hub.domain.TabulationRow
import com.abutorab.teacher.hub.domain.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksheetScreen(viewModel: TeacherViewModel) {
    val searchQuery by viewModel.marksheetSearchQuery.collectAsStateWithLifecycle()
    val marksheet by viewModel.searchedMarksheet.collectAsStateWithLifecycle()
    val allSubjectsRaw by viewModel.allSubjects.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    
    val allSubjects = remember(allSubjectsRaw) {
        allSubjectsRaw.sortedBy { subj ->
            val idx = com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.indexOfFirst { it.id == subj.id }
            if (idx == -1) Int.MAX_VALUE else idx
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Search Marksheet",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onMarksheetSearchChanged,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Enter Student Roll Number") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            singleLine = true
        )

        if (marksheet != null) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                MarksheetCard(marksheet!!, allSubjects)
            }
        } else if (searchQuery.isNotEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No student found with Roll $searchQuery")
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Search for a student to view their marksheet")
            }
        }
    }
}

@Composable
fun MarksheetCard(row: TabulationRow, allSubjects: List<SubjectEntity>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Abutorab M.L. High School", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Mirsarai, Chattogram", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Text(row.student.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Roll ${row.student.rollNumber}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryStat("Total", row.totalMarks.toString())
                val isNoMarks = row.finalGrade == "-"
                SummaryStat("GPA", if (isNoMarks) "-" else row.finalGpa.toString())
                SummaryStat(
                    "Grade",
                    row.finalGrade,
                    valueColor = if (row.finalGpa == 0.0 && !isNoMarks) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
                SummaryStat("Rank", row.meritPosition.toString())
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                LedgerHeaderCell("Subject", Modifier.weight(1f), TextAlign.Start)
                LedgerHeaderCell("MCQ", Modifier.width(42.dp))
                LedgerHeaderCell("Wri", Modifier.width(42.dp))
                LedgerHeaderCell("Pra", Modifier.width(42.dp))
                LedgerHeaderCell("Total", Modifier.width(46.dp))
                LedgerHeaderCell("Grade", Modifier.width(44.dp))
            }
            HorizontalDivider()
            
            allSubjects.forEachIndexed { index, subj ->
                val sr = row.results[subj.id]
                val isFailed = sr?.grade?.point == 0.0 && sr.total > 0
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        subj.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (sr == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    LedgerValueCell(sr?.mcq?.toString() ?: "-", Modifier.width(42.dp))
                    LedgerValueCell(sr?.written?.toString() ?: "-", Modifier.width(42.dp))
                    LedgerValueCell(sr?.practical?.toString() ?: "-", Modifier.width(42.dp))
                    LedgerValueCell(
                        if (sr == null || sr.total == 0) "-" else "${sr.total}",
                        Modifier.width(46.dp),
                        color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        bold = sr != null
                    )
                    LedgerValueCell(
                        sr?.grade?.letter ?: "-",
                        Modifier.width(44.dp),
                        color = if (isFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        bold = sr != null
                    )
                }
                if (index < allSubjects.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            }
            HorizontalDivider(thickness = 2.dp)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Grand Total-",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
                LedgerValueCell("", Modifier.width(42.dp))
                LedgerValueCell("", Modifier.width(42.dp))
                LedgerValueCell("", Modifier.width(42.dp))
                LedgerValueCell("${row.totalMarks}", Modifier.width(46.dp), bold = true)
                LedgerValueCell("", Modifier.width(44.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun SummaryStat(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.tertiary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LedgerHeaderCell(text: String, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Center) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = align,
        modifier = modifier
    )
}

@Composable
private fun LedgerValueCell(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface, bold: Boolean = false) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
    )
}
