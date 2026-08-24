package com.abutorab.teacher.hub.ui.screens

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abutorab.teacher.hub.data.SubjectEntity
import com.abutorab.teacher.hub.domain.TeacherViewModel
import com.abutorab.teacher.hub.network.ScanResultItem
import com.abutorab.teacher.hub.network.ScannerUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditScreen(viewModel: TeacherViewModel) {
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val data by viewModel.activeQuickEditData.collectAsStateWithLifecycle()
    val geminiApiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val selectedYearInt by viewModel.selectedYearInt.collectAsStateWithLifecycle()
    val selectedTerm by viewModel.selectedTerm.collectAsStateWithLifecycle()
    val allMarks by viewModel.allMarks.collectAsStateWithLifecycle()

    val animatedSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.surfaceVariant, animationSpec = tween(300))
    val animatedOnSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurfaceVariant, animationSpec = tween(300))
    val animatedOnSurface by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurface, animationSpec = tween(300))
    val animatedSurface by animateColorAsState(targetValue = MaterialTheme.colorScheme.surface, animationSpec = tween(300))
    val animatedPrimary by animateColorAsState(targetValue = MaterialTheme.colorScheme.primary, animationSpec = tween(300))

    var isScanning by remember { mutableStateOf(false) }
    var scanResults by remember { mutableStateOf<List<ScanResultItem>?>(null) }
    var showVerificationSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isScanning = true
            coroutineScope.launch {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                val results = ScannerUtil.scanMarksheet(bitmap, geminiApiKey)
                isScanning = false
                if (results == null) {
                    Toast.makeText(context, "Failed to scan. Please try again.", Toast.LENGTH_LONG).show()
                } else if (results.isEmpty()) {
                    Toast.makeText(context, "No marks found, or API Key is missing in Settings.", Toast.LENGTH_LONG).show()
                } else {
                    scanResults = results
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(animatedSurfaceVariant)) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Quick Edit Marks",
                style = MaterialTheme.typography.headlineMedium,
                color = animatedOnSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (geminiApiKey.isEmpty()) {
                        Toast.makeText(context, "Please configure your Gemini API Key in Settings first.", Toast.LENGTH_LONG).show()
                    } else {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                }) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Marksheet", tint = animatedPrimary)
                }
            }
        }

        if (showVerificationSheet && selectedSubject != null) {
            VerificationSheetDialog(
                year = selectedYearInt,
                term = selectedTerm,
                subject = selectedSubject!!,
                students = allStudents,
                marks = allMarks.filter { it.subjectId == selectedSubject!!.id },
                onDismiss = { showVerificationSheet = false }
            )
        }

        if (isScanning) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (scanResults != null) {
            AIScanDialog(
                results = scanResults!!,
                onDismiss = { scanResults = null },
                onApplyAll = { results ->
                    results.forEach { res ->
                        viewModel.saveMark(res.roll, res.mcq, res.written, res.practical)
                    }
                    scanResults = null
                }
            )
        }

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
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                label = { Text("Select Subject", color = animatedOnSurfaceVariant) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = animatedOnSurface,
                    unfocusedTextColor = animatedOnSurface,
                    focusedContainerColor = animatedSurface,
                    unfocusedContainerColor = animatedSurface,
                    focusedBorderColor = animatedPrimary,
                    unfocusedBorderColor = animatedOnSurfaceVariant
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                allSubjects.forEach { subj ->
                    DropdownMenuItem(
                        text = { Text(subj.title, color = animatedOnSurface) },
                        onClick = {
                            viewModel.selectSubject(subj.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Students List
        val currentSubject = selectedSubject
        if (currentSubject != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${data.size} Students • ${allMarks.count { it.subjectId == currentSubject.id }} Marks Entered",
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedOnSurfaceVariant
                )
                FilledTonalButton(
                    onClick = { showVerificationSheet = true },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.FactCheck,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("নম্বর ফর্দ (Verify)", style = MaterialTheme.typography.labelMedium)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().imePadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(data, key = { it.student.rollNumber }) { item ->
                    StudentMarkRow(
                        item = item,
                        subject = currentSubject,
                        onMarkChanged = { mcq, written, practical ->
                            viewModel.saveMark(item.student.rollNumber, mcq, written, practical)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Please select a subject above", color = animatedOnSurfaceVariant)
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun StudentMarkRow(
    item: com.abutorab.teacher.hub.domain.StudentMarkRow,
    subject: SubjectEntity,
    onMarkChanged: (Int?, Int?, Int?) -> Unit
) {
    var flashTrigger by remember { mutableStateOf(0) }
    var isInitialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(item.mark) {
        if (!isInitialLoad) {
            flashTrigger++
        }
        isInitialLoad = false
    }

    val defaultColor = MaterialTheme.colorScheme.surface
    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    var isHighlight by remember { mutableStateOf(false) }

    LaunchedEffect(flashTrigger) {
        if (flashTrigger > 0) {
            isHighlight = true
            delay(700)
            isHighlight = false
        }
    }

    val targetColor = if (isHighlight) highlightColor else defaultColor
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 300)
    )

    val animatedPrimaryContainer by animateColorAsState(targetValue = MaterialTheme.colorScheme.primaryContainer, animationSpec = tween(300))
    val animatedOnPrimaryContainer by animateColorAsState(targetValue = MaterialTheme.colorScheme.onPrimaryContainer, animationSpec = tween(300))
    val animatedOnSurface by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurface, animationSpec = tween(300))
    val animatedOnSurfaceVariant by animateColorAsState(targetValue = MaterialTheme.colorScheme.onSurfaceVariant, animationSpec = tween(300))
    val animatedPrimary by animateColorAsState(targetValue = MaterialTheme.colorScheme.primary, animationSpec = tween(300))

    val cardRequester = remember { BringIntoViewRequester() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(cardRequester),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = animatedColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(animatedPrimaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.student.rollNumber.toString(),
                        fontWeight = FontWeight.Bold,
                        color = animatedOnPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${item.student.name} (Roll: ${item.student.rollNumber})",
                    style = MaterialTheme.typography.titleMedium,
                    color = animatedOnSurface
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                var mcqText by remember(item.mark?.mcq) { mutableStateOf(item.mark?.mcq?.toString() ?: "") }
                var writtenText by remember(item.mark?.written) { mutableStateOf(item.mark?.written?.toString() ?: "") }
                var practicalText by remember(item.mark?.practical) { mutableStateOf(item.mark?.practical?.toString() ?: "") }

                var mcqFocused by remember { mutableStateOf(false) }
                var writtenFocused by remember { mutableStateOf(false) }
                var practicalFocused by remember { mutableStateOf(false) }

                LaunchedEffect(mcqFocused, writtenFocused, practicalFocused) {
                    if (mcqFocused || writtenFocused || practicalFocused) {
                        cardRequester.bringIntoView()
                    }
                }

                if (subject.hasMcq) {
                    OutlinedTextField(
                        value = mcqText,
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (newVal.isEmpty() || (parsed != null && parsed <= subject.maxMcq)) {
                                mcqText = newVal
                                onMarkChanged(newVal.toIntOrNull(), writtenText.toIntOrNull(), practicalText.toIntOrNull())
                            }
                        },
                        label = { Text("MCQ (${subject.maxMcq})", color = animatedOnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusEvent { mcqFocused = it.isFocused },
                        singleLine = true,
                        isError = (mcqText.toIntOrNull() ?: 0) > subject.maxMcq,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = animatedOnSurface,
                            unfocusedTextColor = animatedOnSurface,
                            focusedBorderColor = animatedPrimary,
                            unfocusedBorderColor = animatedOnSurfaceVariant,
                            focusedLabelColor = animatedPrimary,
                            unfocusedLabelColor = animatedOnSurfaceVariant
                        )
                    )
                }

                if (subject.hasWritten) {
                    OutlinedTextField(
                        value = writtenText,
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (newVal.isEmpty() || (parsed != null && parsed <= subject.maxWritten)) {
                                writtenText = newVal
                                onMarkChanged(mcqText.toIntOrNull(), newVal.toIntOrNull(), practicalText.toIntOrNull())
                            }
                        },
                        label = { Text("Written (${subject.maxWritten})", color = animatedOnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusEvent { writtenFocused = it.isFocused },
                        singleLine = true,
                        isError = (writtenText.toIntOrNull() ?: 0) > subject.maxWritten,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = animatedOnSurface,
                            unfocusedTextColor = animatedOnSurface,
                            focusedBorderColor = animatedPrimary,
                            unfocusedBorderColor = animatedOnSurfaceVariant,
                            focusedLabelColor = animatedPrimary,
                            unfocusedLabelColor = animatedOnSurfaceVariant
                        )
                    )
                }

                if (subject.hasPractical) {
                    OutlinedTextField(
                        value = practicalText,
                        onValueChange = { newVal ->
                            val parsed = newVal.toIntOrNull()
                            if (newVal.isEmpty() || (parsed != null && parsed <= subject.maxPractical)) {
                                practicalText = newVal
                                onMarkChanged(mcqText.toIntOrNull(), writtenText.toIntOrNull(), newVal.toIntOrNull())
                            }
                        },
                        label = { Text("Prac (${subject.maxPractical})", color = animatedOnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusEvent { practicalFocused = it.isFocused },
                        singleLine = true,
                        isError = (practicalText.toIntOrNull() ?: 0) > subject.maxPractical,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = animatedOnSurface,
                            unfocusedTextColor = animatedOnSurface,
                            focusedBorderColor = animatedPrimary,
                            unfocusedBorderColor = animatedOnSurfaceVariant,
                            focusedLabelColor = animatedPrimary,
                            unfocusedLabelColor = animatedOnSurfaceVariant
                        )
                    )
                }
            }
            if (item.mark?.mcq == null && item.mark?.written == null && item.mark?.practical == null) {
                Text(
                    text = "No marks entered",
                    style = MaterialTheme.typography.bodySmall,
                    color = animatedOnSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
