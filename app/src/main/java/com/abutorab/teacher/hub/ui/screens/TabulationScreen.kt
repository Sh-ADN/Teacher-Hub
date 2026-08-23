package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.domain.TeacherViewModel
import com.abutorab.teacher.hub.util.NumeralFormat

private val FailRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TabulationScreen(viewModel: TeacherViewModel, onNavigateToMarksheet: (Int) -> Unit = {}) {
    val tabulationData by viewModel.tabulationData.collectAsStateWithLifecycle()
    val allSubjectsRaw by viewModel.allSubjects.collectAsStateWithLifecycle()
    val allSubjects = remember(allSubjectsRaw) {
        allSubjectsRaw.sortedBy { subj ->
            val idx = com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.indexOfFirst { it.id == subj.id }
            if (idx == -1) Int.MAX_VALUE else idx
        }
    }

    val summaryColumns = remember { listOf("মোট" to 64.dp, "জিপিএ" to 56.dp, "গ্রেড" to 56.dp, "মেধা স্থান" to 64.dp) }
    val horizontalScrollState = rememberScrollState()
    var selectedStudentId by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "ট্যাবুলেশন শিট",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (tabulationData.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data available.")
            }
            return@Column
        }

        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh).padding(vertical = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                HeaderCell("রোল", 44.dp)
                HeaderCell("নাম", 120.dp, alignStart = true)
                Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                    allSubjects.forEach { subj -> 
                        val bengaliTitle = com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.find { it.id == subj.id }?.bengaliTitle ?: subj.title
                        val width = if (subj.hasMcq && subj.hasWritten && subj.hasPractical) 152.dp else if ((subj.hasMcq && subj.hasWritten) || (subj.hasWritten && subj.hasPractical) || (subj.hasMcq && subj.hasPractical)) 112.dp else 76.dp
                        HeaderCell(bengaliTitle, width) 
                    }
                    summaryColumns.forEach { (label, width) -> HeaderCell(label, width) }
                }
            }
            HorizontalDivider()
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(tabulationData, key = { it.student.rollNumber }) { rowData ->
                    val isSelected = selectedStudentId == rowData.student.rollNumber
                    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        selectedStudentId = if (isSelected) null else rowData.student.rollNumber
                                    },
                                    onDoubleTap = {
                                        onNavigateToMarksheet(rowData.student.rollNumber)
                                    }
                                )
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DataCell(rowData.student.rollNumber.toString(), 44.dp)
                        DataCell(rowData.student.name, 120.dp, alignStart = true, localize = false)
                        Row(modifier = Modifier.weight(1f).horizontalScroll(horizontalScrollState)) {
                            allSubjects.forEach { subj ->
                                val width = if (subj.hasMcq && subj.hasWritten && subj.hasPractical) 152.dp else if ((subj.hasMcq && subj.hasWritten) || (subj.hasWritten && subj.hasPractical) || (subj.hasMcq && subj.hasPractical)) 112.dp else 76.dp
                                val sr = rowData.results[subj.id]
                                val textParts = mutableListOf<String>()
                                if (subj.hasMcq) textParts.add(sr?.mcq?.toString() ?: "-")
                                if (subj.hasWritten) textParts.add(sr?.written?.toString() ?: "-")
                                if (subj.hasPractical) textParts.add(sr?.practical?.toString() ?: "-")
                                val text = textParts.joinToString("+") + if (textParts.size > 1) "=${sr?.total ?: "-"}" else ""

                                val isFail = sr?.grade?.point == 0.0 && sr.total > 0
                                val cellColor = if (isFail) FailRed else MaterialTheme.colorScheme.onSurface
                                DataCell(text, width, color = cellColor)
                            }
                            DataCell(rowData.totalMarks.toString(), 64.dp)
                            val isNoMarks = rowData.finalGrade == "-"
                            DataCell(if (isNoMarks) "-" else rowData.finalGpa.toString(), 56.dp, color = MaterialTheme.colorScheme.tertiary, localize = false)
                            DataCell(
                                rowData.finalGrade,
                                56.dp,
                                color = if (rowData.finalGpa == 0.0 && !isNoMarks) FailRed else MaterialTheme.colorScheme.onSurface,
                                localize = false
                            )
                            DataCell(rowData.meritPosition.toString(), 64.dp)
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp, alignStart: Boolean = false) {
    Box(modifier = Modifier.width(width).padding(horizontal = 3.dp), contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = if (alignStart) TextAlign.Start else TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DataCell(text: String, width: Dp, alignStart: Boolean = false, color: Color = MaterialTheme.colorScheme.onSurface, localize: Boolean = true) {
    Box(modifier = Modifier.width(width).padding(horizontal = 3.dp), contentAlignment = if (alignStart) Alignment.CenterStart else Alignment.Center) {
        Text(
            NumeralFormat.localize(text, localize),
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            textAlign = if (alignStart) TextAlign.Start else TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
