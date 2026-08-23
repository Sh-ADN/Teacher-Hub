package com.abutorab.teacher.hub.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.abutorab.teacher.hub.network.ScanResultItem

@Composable
fun AIScanDialog(
    results: List<ScanResultItem>,
    onDismiss: () -> Unit,
    onApplyAll: (List<ScanResultItem>) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Review Scanned Marks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            Text("Roll", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("MCQ", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("Written", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Text("Prac", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider()
                    }
                    items(results) { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Text(item.roll.toString(), modifier = Modifier.weight(1f))
                            Text(item.mcq?.toString() ?: "-", modifier = Modifier.weight(1f))
                            Text(item.written?.toString() ?: "-", modifier = Modifier.weight(1f))
                            Text(item.practical?.toString() ?: "-", modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider()
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onApplyAll(results) }) {
                        Text("Save All")
                    }
                }
            }
        }
    }
}
