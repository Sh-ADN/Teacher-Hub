package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeritListScreen(viewModel: TeacherViewModel, onBack: () -> Unit) {
    val tabulationData by viewModel.tabulationData.collectAsStateWithLifecycle()
    
    // Sort by Total Marks (descending) as requested by user.
    // However, if we want to follow standard Merit logic, we usually sort by GPA then Total Marks.
    // Since user specifically said "The list should be sorted by Total Marks (descending)", we'll do that,
    // but typically TabulationRow.meritPosition already handles the correct sort logic.
    // So let's just sort by meritPosition which represents the correct rank.
    val sortedStudents = tabulationData.filter { it.finalGrade != "-" }.sortedBy { it.meritPosition }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        
        if (sortedStudents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No data available.")
            }
        } else {
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
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
