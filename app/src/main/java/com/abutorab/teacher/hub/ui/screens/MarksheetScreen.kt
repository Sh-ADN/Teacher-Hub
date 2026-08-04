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
import com.abutorab.teacher.hub.util.NumeralFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksheetScreen(viewModel: TeacherViewModel) {
    val searchQuery by viewModel.marksheetSearchQuery.collectAsStateWithLifecycle()
    val marksheet by viewModel.searchedMarksheet.collectAsStateWithLifecycle()
    val allSubjectsRaw by viewModel.allSubjects.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    
    val allSubjects = remember(allSubjectsRaw) {
        allSubjectsRaw.toList()
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
            // Remove vertical scroll from here so header can be pinned inside MarksheetCard
            Column(modifier = Modifier.weight(1f)) {
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

enum class SubjectCategory(val defaultName: String) {
    BANGLA_1("বাংলা ১ম"),
    BANGLA_2("বাংলা ২য়"),
    ENGLISH_1("ইংরেজি ১ম"),
    ENGLISH_2("ইংরেজি ২য়"),
    MATH("গণিত"),
    RELIGION("ধর্মীয় শিক্ষা"),
    AGRICULTURE("কৃষি শিক্ষা"),
    SCIENCE("সাধারণ বিজ্ঞান"),
    BGS("বাংলাদেশ ও বিশ্বপরিচয়"),
    HISTORY("ইতিহাস"),
    GEOGRAPHY("ভূগোল"),
    CIVICS("পৌরনীতি"),
    FINANCE("ফাইন্যান্স ও ব্যাংকিং"),
    ACCOUNTING("হিসাব বিজ্ঞান"),
    ENTREPRENEURSHIP("ব্যবসায় উদ্যোগ"),
    PHYSICS("পদার্থ বিজ্ঞান"),
    CHEMISTRY("রসায়ন বিজ্ঞান"),
    BIOLOGY("জীব বিজ্ঞান"),
    HIGHER_MATH("উচ্চতর গণিত"),
    ICT("তথ্য ও যোগাযোগ প্রযুক্তি")
}

fun findSubjectForCategory(category: SubjectCategory, allSubjects: List<SubjectEntity>): SubjectEntity? {
    return allSubjects.find { s ->
        val t = s.title.lowercase()
        val id = s.id
        when (category) {
            SubjectCategory.BANGLA_1 -> id == "101" || (t.contains("bangla") && t.contains("1")) || t.contains("বাংলা ১ম")
            SubjectCategory.BANGLA_2 -> id == "102" || (t.contains("bangla") && t.contains("2")) || t.contains("বাংলা ২য়")
            SubjectCategory.ENGLISH_1 -> id == "107" || (t.contains("english") && t.contains("1")) || t.contains("ইংরেজি ১ম")
            SubjectCategory.ENGLISH_2 -> id == "108" || (t.contains("english") && t.contains("2")) || t.contains("ইংরেজি ২য়")
            SubjectCategory.MATH -> (id == "109" || t.contains("math") || t.contains("গণিত")) && !t.contains("higher") && !t.contains("উচ্চতর")
            SubjectCategory.RELIGION -> id == "111" || t.contains("religion") || t.contains("islam") || t.contains("hindu") || t.contains("buddh") || t.contains("ধর্ম") || t.contains("ইসলাম") || t.contains("হিন্দু") || t.contains("বৌদ্ধ")
            SubjectCategory.AGRICULTURE -> t.contains("agri") || t.contains("কৃষি")
            SubjectCategory.SCIENCE -> (id == "127" || t.contains("science") || t.contains("বিজ্ঞান")) && !t.contains("physics") && !t.contains("chemistry") && !t.contains("biology") && !t.contains("পদার্থ") && !t.contains("রসায়ন") && !t.contains("জীব") && !t.contains("হিসাব")
            SubjectCategory.BGS -> id == "150" || t.contains("global") || t.contains("বিশ্বপরিচয়")
            SubjectCategory.HISTORY -> t.contains("history") || t.contains("ইতিহাস")
            SubjectCategory.GEOGRAPHY -> t.contains("geography") || t.contains("ভূগোল")
            SubjectCategory.CIVICS -> t.contains("civics") || t.contains("পৌরনীতি")
            SubjectCategory.FINANCE -> t.contains("finance") || t.contains("ফাইন্যান্স")
            SubjectCategory.ACCOUNTING -> t.contains("accounting") || t.contains("হিসাব")
            SubjectCategory.ENTREPRENEURSHIP -> t.contains("entrepreneurship") || t.contains("উদ্যোগ")
            SubjectCategory.PHYSICS -> t.contains("physics") || t.contains("পদার্থ")
            SubjectCategory.CHEMISTRY -> t.contains("chemistry") || t.contains("রসায়ন")
            SubjectCategory.BIOLOGY -> t.contains("biology") || t.contains("জীব")
            SubjectCategory.HIGHER_MATH -> t.contains("higher math") || t.contains("উচ্চতর")
            SubjectCategory.ICT -> id == "154" || t.contains("ict") || t.contains("information") || t.contains("যোগাযোগ") || t.contains("তথ্য")
        }
    }
}

@Composable
fun MarksheetCard(row: TabulationRow, allSubjects: List<SubjectEntity>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Abutorab M.L. High School", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("Mirsarai, Chattogram", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text(row.student.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("রোল ${NumeralFormat.localize(row.student.rollNumber.toString())}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SummaryStat("মোট", row.totalMarks.toString(), localize = true)
                    val isNoMarks = row.finalGrade == "-"
                    SummaryStat("জিপিএ", if (isNoMarks) "-" else row.finalGpa.toString(), localize = false)
                    SummaryStat(
                        "গ্রেড",
                        row.finalGrade,
                        valueColor = if (row.finalGpa == 0.0 && !isNoMarks) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        localize = false
                    )
                    SummaryStat("মেধা স্থান", row.meritPosition.toString(), localize = true)
                }
            }
        }
        
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(top = 8.dp)) {
                // Fixed Header Row
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    LedgerHeaderCell("বিষয়", Modifier.weight(1f), TextAlign.Start)
                    LedgerHeaderCell("নৈর্ব্যঃ", Modifier.width(42.dp))
                    LedgerHeaderCell("রচনা", Modifier.width(42.dp))
                    LedgerHeaderCell("ব্যঃ", Modifier.width(42.dp))
                    LedgerHeaderCell("মোট", Modifier.width(46.dp))
                    LedgerHeaderCell("গ্রেড", Modifier.width(44.dp))
                }
                HorizontalDivider()
                
                // Scrollable Subject Rows
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    val categories = SubjectCategory.values()
                    
                    categories.forEachIndexed { index, category ->
                        val matchedSubject = findSubjectForCategory(category, allSubjects)
                        val sr = if (matchedSubject != null) row.results[matchedSubject.id] else null
                        
                        val displayName = category.defaultName
                        
                        val isFailed = sr?.grade?.point == 0.0 && sr.total > 0
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                displayName,
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
                                bold = sr != null,
                                localize = false
                            )
                        }
                        if (index < categories.lastIndex) HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
                    }
                    
                    HorizontalDivider(thickness = 2.dp)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "মোট নম্বর-",
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
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryStat(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.tertiary, localize: Boolean = true) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            NumeralFormat.localize(value, localize),
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
private fun LedgerValueCell(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface, bold: Boolean = false, localize: Boolean = true) {
    Text(
        NumeralFormat.localize(text, localize),
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textAlign = TextAlign.Center,
        modifier = modifier,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
    )
}
