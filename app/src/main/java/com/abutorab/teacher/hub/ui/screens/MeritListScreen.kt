package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.domain.TeacherViewModel
import com.abutorab.teacher.hub.domain.TabulationRow
import com.abutorab.teacher.hub.domain.SubjectResult

data class SubjectTopper(
    val rank: Int,
    val roll: Int,
    val name: String,
    val breakdown: String,
    val letterGrade: String
)

data class SubjectTopperGroup(
    val subjectId: String,
    val subjectName: String,
    val fullMarks: Int,
    val toppers: List<SubjectTopper>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeritListScreen(viewModel: TeacherViewModel, onBack: () -> Unit) {
    val tabulationData by viewModel.tabulationData.collectAsStateWithLifecycle()
    val allSubjectsRaw by viewModel.allSubjects.collectAsStateWithLifecycle()
    
    val sortedStudents = tabulationData.filter { it.finalGrade != "-" }.sortedBy { it.meritPosition }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 0.dp)) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Merit List") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Subject Toppers") })
        }
        
        if (sortedStudents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data available.")
            }
        } else {
            if (selectedTab == 0) {
                MeritListTab(sortedStudents)
            } else {
                SubjectToppersTab(sortedStudents, allSubjectsRaw)
            }
        }
    }
}

@Composable
fun MeritListTab(sortedStudents: List<TabulationRow>) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Rank", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                    Text("Roll", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
                    Text("Name", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Start, modifier = Modifier.weight(1f))
                    Text("Total", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(52.dp))
                    Text("GPA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                    Text("Grade", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                }
                HorizontalDivider()
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(sortedStudents) { s ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val medalColor = when (s.meritPosition) {
                                1 -> MaterialTheme.colorScheme.tertiary
                                2, 3 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                            Text(s.meritPosition.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = medalColor, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                            Text(s.student.rollNumber.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
                            Text(s.student.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text(s.totalMarks.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(52.dp))
                            Text(if (s.failedSubjectCount > 0) "-" else "%.2f".format(s.finalGpa), style = MaterialTheme.typography.bodySmall, color = if (s.failedSubjectCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                            Text(s.finalGrade, style = MaterialTheme.typography.bodySmall, color = if (s.failedSubjectCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectToppersTab(students: List<TabulationRow>, allSubjectsRaw: List<com.abutorab.teacher.hub.data.SubjectEntity>) {
    var topN by remember { mutableIntStateOf(3) }
    
    val subjectGroups = remember(students, topN, allSubjectsRaw) {
        allSubjectsRaw.mapNotNull { subject ->
            val validResults = students.mapNotNull { row ->
                val sr = row.results[subject.id]
                if (sr != null && (sr.mcq != null || sr.written != null || sr.practical != null)) {
                    Pair(row, sr)
                } else null
            }
            if (validResults.isEmpty()) return@mapNotNull null
            
            // Sort by subject total descending
            val sorted = validResults.sortedByDescending { it.second.total }
            
            val toppers = mutableListOf<SubjectTopper>()
            var currentRank = 1
            var countAtCurrentRank = 0
            var previousTotal = -1
            
            for ((row, sr) in sorted) {
                if (sr.total != previousTotal) {
                    if (toppers.size >= topN) break
                    currentRank += countAtCurrentRank
                    countAtCurrentRank = 1
                    previousTotal = sr.total
                } else {
                    countAtCurrentRank++
                }
                
                if (currentRank > topN && toppers.isNotEmpty() && sr.total != toppers.last().breakdown.substringAfter("=").toIntOrNull()) break

                val parts = mutableListOf<String>()
                if (subject.hasMcq) parts.add((sr.mcq ?: 0).toString())
                if (subject.hasWritten) parts.add((sr.written ?: 0).toString())
                if (subject.hasPractical) parts.add((sr.practical ?: 0).toString())
                
                val breakdown = if (parts.size > 1) {
                    parts.joinToString("+") + "=${sr.total}"
                } else {
                    sr.total.toString()
                }

                toppers.add(SubjectTopper(
                    rank = currentRank,
                    roll = row.student.rollNumber,
                    name = row.student.name,
                    breakdown = breakdown,
                    letterGrade = sr.grade.letter
                ))
            }
            
            SubjectTopperGroup(
                subjectId = subject.id,
                subjectName = bengaliSubjectLabel(subject.id, subject.title),
                fullMarks = subject.maxMarks,
                toppers = toppers
            )
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Toppers per subject", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 3, 5).forEach { n ->
                            FilterChip(
                                selected = topN == n,
                                onClick = { topN = n },
                                label = { Text("Top $n") }
                            )
                        }
                    }
                }
            }
        }
        
        items(subjectGroups) { group ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            group.subjectName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text("/${group.fullMarks}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    if (group.toppers.isEmpty()) {
                        Text("No marks entered yet", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        group.toppers.forEachIndexed { index, topper ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "#${topper.rank}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (topper.rank == 1) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text("Roll ${topper.roll}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(72.dp))
                                Text(topper.name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text(topper.breakdown, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
                                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)) {
                                    Text(topper.letterGrade, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            if (index < group.toppers.lastIndex) HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
