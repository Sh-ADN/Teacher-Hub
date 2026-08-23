package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.domain.TeacherViewModel
import com.abutorab.teacher.hub.domain.TabulationRow
import com.abutorab.teacher.hub.util.NumeralFormat
import kotlin.math.max

data class SubjectStatInfo(
    val id: String, val title: String, val avgPercent: Double, val avgScore: Double,
    val highest: Int, val lowest: Int, val passed: Int, val failed: Int
)

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

    val averageTotal = if (evaluatedStudents.isNotEmpty()) evaluatedStudents.map { it.totalMarks }.average() else 0.0
    val sortedTotals = evaluatedStudents.map { it.totalMarks }.sorted()
    val medianTotal = if (sortedTotals.isNotEmpty()) {
        if (sortedTotals.size % 2 == 0) {
            (sortedTotals[sortedTotals.size / 2 - 1] + sortedTotals[sortedTotals.size / 2]) / 2.0
        } else {
            sortedTotals[sortedTotals.size / 2].toDouble()
        }
    } else 0.0
    val highestMark = if (evaluatedStudents.isNotEmpty()) evaluatedStudents.maxOf { it.totalMarks } else 0
    val classAvgGpa = if (evaluatedStudents.isNotEmpty()) evaluatedStudents.map { it.finalGpa }.average() else 0.0

    val grades = listOf("A+", "A", "A-", "B", "C", "D", "F")
    val gradeCounts = evaluatedStudents.groupingBy { it.finalGrade }.eachCount()
    val gradeDistribution = grades.associateWith { gradeCounts[it] ?: 0 }

    val marksByRoll = evaluatedStudents.sortedBy { it.student.rollNumber }.map { Pair(it.student.rollNumber, it.totalMarks) }.take(50) // limit for UI

    val top10 = evaluatedStudents.sortedBy { it.meritPosition }.take(10)
    val bottom10 = evaluatedStudents.sortedByDescending { it.meritPosition }.take(10).reversed()

    val subjectStats = allSubjectsRaw.map { subject ->
        val subjectResults = evaluatedStudents.mapNotNull { it.results[subject.id] }.filter { (it.mcq != null || it.written != null || it.practical != null) }
        val avgScore = if (subjectResults.isNotEmpty()) subjectResults.map { it.total }.average() else 0.0
        val avgPercent = if (subject.maxMarks > 0) (avgScore / subject.maxMarks) * 100 else 0.0
        val highest = if (subjectResults.isNotEmpty()) subjectResults.maxOf { it.total } else 0
        val lowest = if (subjectResults.isNotEmpty()) subjectResults.minOf { it.total } else 0
        val passed = subjectResults.count { it.grade.point > 0.0 }
        val failed = subjectResults.count { it.grade.point == 0.0 }
        SubjectStatInfo(subject.id, subject.title, avgPercent, avgScore, highest, lowest, passed, failed)
    }.filter { it.passed > 0 || it.failed > 0 }.sortedByDescending { it.avgPercent }

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
                Text("Core Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard("Total Students", totalStudents.toString(), Modifier.weight(1f))
                        StatCard("Passed", totalPassed.toString(), Modifier.weight(1f), accentColor = MaterialTheme.colorScheme.tertiary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard("Failed", totalFailed.toString(), Modifier.weight(1f), accentColor = MaterialTheme.colorScheme.error)
                        StatCard("Pass Rate", "%.1f%%".format(passPercentage), Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard("Average Total", "%.1f".format(averageTotal), Modifier.weight(1f))
                        StatCard("Median Total", "%.1f".format(medianTotal), Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard("Highest Mark", highestMark.toString(), Modifier.weight(1f))
                        StatCard("Class Avg GPA", "%.2f".format(classAvgGpa), Modifier.weight(1f))
                    }
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
                Text("Performance Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Students per Grade", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        GradeBarChart(gradeDistribution, MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Text("Total Marks by Roll", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MarksLineChart(marksByRoll, MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                Text("Subject-wise Deep Analysis", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            if (hardestSubject != null) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Hardest Subject: ${bengaliSubjectLabel(hardestSubject.id, hardestSubject.title)} (%.1f%% Avg)".format(hardestSubject.avgPercent),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
            item {
                SubjectStatsTable(subjectStats)
            }

            item {
                Text("Ranking & Merit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                    Text("View Full Merit List & Toppers")
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

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
private fun GradeBarChart(distribution: Map<String, Int>, barColor: Color) {
    val maxValue = (distribution.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        distribution.forEach { (grade, count) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).fillMaxHeight()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    val fraction = count.toFloat() / maxValue.toFloat()
                    Canvas(modifier = Modifier.fillMaxWidth(0.5f).fillMaxHeight(fraction.coerceIn(0.02f, 1f))) {
                        drawRect(color = barColor)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = labelColor)
                Text(grade, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = labelColor)
            }
        }
    }
}

@Composable
private fun MarksLineChart(data: List<Pair<Int, Int>>, lineColor: Color) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    if (data.isEmpty()) return
    val maxTotal = (data.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    val itemWidth = 36.dp
    val totalWidth = itemWidth * data.size
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp - 32.dp
    val chartWidth = androidx.compose.ui.unit.max(totalWidth, screenWidth)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.width(chartWidth)) {
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val stepX = if (data.size > 1) size.width / (data.size - 1) else 0f
                val points = data.mapIndexed { index, (_, total) ->
                    val x = index * stepX
                    val y = size.height - (total.toFloat() / maxTotal.toFloat()) * size.height
                    Offset(x, y)
                }
                for (i in 0 until points.size - 1) {
                    drawLine(color = lineColor, start = points[i], end = points[i + 1], strokeWidth = 4f)
                }
                points.forEach { point ->
                    drawCircle(color = lineColor, radius = 6f, center = point)
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                data.forEach { (roll, _) ->
                    Text(
                        text = roll.toString(), 
                        style = MaterialTheme.typography.labelSmall, 
                        color = labelColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectStatsTable(subjects: List<SubjectStatInfo>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Subject", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Start, modifier = Modifier.weight(1f))
                Text("Avg", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(56.dp))
                Text("High", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
                Text("Low", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
                Text("Pass", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                Text("Fail", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
            }
            HorizontalDivider()
            subjects.forEachIndexed { index, s ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(bengaliSubjectLabel(s.id, s.title), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("%.1f".format(s.avgScore), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.width(56.dp))
                    Text(s.highest.toString(), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
                    Text(s.lowest.toString(), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.width(44.dp))
                    Text(s.passed.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                    Text(s.failed.toString(), style = MaterialTheme.typography.bodySmall, color = if (s.failed > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(40.dp))
                }
                if (index < subjects.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
            }
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
