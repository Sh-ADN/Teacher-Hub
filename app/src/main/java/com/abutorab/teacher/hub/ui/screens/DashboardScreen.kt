package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.domain.TeacherViewModel
import com.abutorab.teacher.hub.domain.TabulationRow
import androidx.compose.ui.graphics.Color
import com.abutorab.teacher.hub.util.NumeralFormat
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: TeacherViewModel, onNavigateToMeritList: () -> Unit) {
    val tabulationData by viewModel.tabulationData.collectAsStateWithLifecycle()
    val allSubjectsRaw by viewModel.allSubjects.collectAsStateWithLifecycle()

    val totalStudents = tabulationData.size
    val evaluatedStudents = tabulationData.filter { it.finalGrade != "-" }
    val totalPassed = evaluatedStudents.count { it.failedSubjectCount == 0 }
    val totalFailed = evaluatedStudents.count { it.failedSubjectCount > 0 }
    val passPercentage = if (evaluatedStudents.isNotEmpty()) (totalPassed.toFloat() / evaluatedStudents.size) * 100 else 0f

    val top10 = evaluatedStudents.sortedBy { it.meritPosition }.take(10)
    val bottom10 = evaluatedStudents.sortedByDescending { it.meritPosition }.take(10)

    val subjectStats = allSubjectsRaw.map { subject ->
        val subjectResults = evaluatedStudents.mapNotNull { it.results[subject.id] }.filter { (it.mcq != null || it.written != null || it.practical != null) }
        val avgScore = if (subjectResults.isNotEmpty()) subjectResults.map { it.total }.average() else 0.0
        val avgPercent = if (subject.maxMarks > 0) (avgScore / subject.maxMarks) * 100 else 0.0
        SubjectStatInfo(subject.id, subject.title, avgPercent)
    }.filter { it.avgPercent > 0.0 }.sortedByDescending { it.avgPercent }

    val easiestSubject = subjectStats.firstOrNull()
    val hardestSubject = subjectStats.lastOrNull()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) { paddingValues ->
        if (totalStudents == 0) {
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data available.")
            }
            return@Scaffold
        }
        
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Core Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("Total Students", totalStudents.toString(), Modifier.weight(1f))
                    StatCard("Passed", totalPassed.toString(), Modifier.weight(1f), accentColor = MaterialTheme.colorScheme.tertiary)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    StatCard("Failed", totalFailed.toString(), Modifier.weight(1f), accentColor = MaterialTheme.colorScheme.error)
                    StatCard("Pass Rate", "%.1f%%".format(passPercentage), Modifier.weight(1f))
                }
            }
            if (totalStudents > evaluatedStudents.size) {
                item {
                    Text(
                        "${totalStudents - evaluatedStudents.size} student(s) not yet graded — excluded from the stats above",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            item {
                Text("Subject Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    if (easiestSubject != null) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Easiest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(4.dp))
                                Text(bengaliSubjectLabel(easiestSubject.id, easiestSubject.title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, textAlign = TextAlign.Center)
                                Text("%.1f%% Avg".format(easiestSubject.avgPercent), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (hardestSubject != null && hardestSubject.id != easiestSubject?.id) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hardest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(Modifier.height(4.dp))
                                Text(bengaliSubjectLabel(hardestSubject.id, hardestSubject.title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center)
                                Text("%.1f%% Avg".format(hardestSubject.avgPercent), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }

            item {
                Text("Ranking & Merit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item {
                LeaderboardPreview("Top 10 Students", top10, false)
            }
            item {
                LeaderboardPreview("Bottom 10 Students", bottom10, true)
            }
            item {
                FilledTonalButton(
                    onClick = onNavigateToMeritList,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("View Full Merit List")
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

data class SubjectStatInfo(val id: String, val title: String, val avgPercent: Double)

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier, accentColor: Color? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(NumeralFormat.localize(value, false), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accentColor ?: MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun LeaderboardPreview(title: String, students: List<TabulationRow>, showFailedInstead: Boolean) {
    Column {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Rank", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                    Text("Roll", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
                    Text("Name", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Start, modifier = Modifier.weight(1f))
                    Text("Total", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(52.dp))
                    Text(if (showFailedInstead) "Failed" else "GPA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                }
                HorizontalDivider()
                students.forEachIndexed { index, s ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(s.meritPosition.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                        Text(s.student.rollNumber.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
                        Text(s.student.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(s.totalMarks.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(52.dp))
                        if (showFailedInstead) {
                            Text(s.failedSubjectCount.toString(), style = MaterialTheme.typography.bodySmall, color = if (s.failedSubjectCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                        } else {
                            Text("%.2f".format(s.finalGpa), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                        }
                    }
                    if (index < students.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    }
}

fun bengaliSubjectLabel(id: String, title: String): String {
    val predefined = com.abutorab.teacher.hub.domain.PREDEFINED_SUBJECTS.find { it.id == id }
    if (predefined != null) return predefined.bengaliTitle
    return title
}
