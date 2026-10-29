package org.agentos.poc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.agentos.poc.AgentApp
import org.agentos.poc.memory.db.EpisodicSnapshotEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimelineInspectorScreen() {
    val repository = remember { AgentApp.instance.memoryRepository }
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<EpisodicSnapshotEntity>?>(null) }
    val recentSnapshots by repository.recentSnapshotsFlow.collectAsState(initial = emptyList())

    val displayedSnapshots = searchResults ?: recentSnapshots

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Episodic Memory Timeline",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Local-first context index recorded from user apps",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                scope.launch {
                    if (it.isBlank()) {
                        searchResults = null
                    } else {
                        searchResults = repository.searchMemory(it)
                    }
                }
            },
            label = { Text("Search memory (e.g. คอนโด, Grab, ยอดเงิน)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Records: ${displayedSnapshots.size}",
                style = MaterialTheme.typography.labelMedium
            )
            TextButton(
                onClick = {
                    scope.launch { repository.clearAll() }
                }
            ) {
                Text("Clear All Memory", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (displayedSnapshots.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No memory records found.\nOpen other apps while Accessibility is active.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedSnapshots, key = { it.id }) { item ->
                    SnapshotCard(
                        snapshot = item,
                        onDelete = {
                            scope.launch { repository.deleteSnapshot(item.id) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SnapshotCard(
    snapshot: EpisodicSnapshotEntity,
    onDelete: () -> Unit
) {
    val dateString = remember(snapshot.timestampMs) {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(snapshot.timestampMs))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = snapshot.appLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                }
            }

            Text(
                text = "$dateString • ${snapshot.packageName}",
                fontSize = 11.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = snapshot.extractedText.take(280) + if (snapshot.extractedText.length > 280) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4
            )
        }
    }
}

