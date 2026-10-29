package org.agentos.poc.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.agentos.poc.AgentApp
import org.agentos.poc.agent.orchestrator.PromptTemplates
import org.agentos.poc.collector.ContextAccessibilityService
import org.agentos.poc.collector.ShizukuBridge

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val repository = remember { AgentApp.instance.memoryRepository }
    val scope = rememberCoroutineScope()

    var isAccessibilityActive by remember { mutableStateOf(ContextAccessibilityService.getInstance() != null) }
    var isShizukuActive by remember { mutableStateOf(ShizukuBridge.hasPermission()) }

    var userQuestion by remember { mutableStateOf("") }
    var aiAnswer by remember { mutableStateOf<String?>(null) }
    var isQuerying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAccessibilityActive = ContextAccessibilityService.getInstance() != null
        isShizukuActive = ShizukuBridge.hasPermission()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Agentic Mobile OS",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "System-level Co-pilot & Autonomous Agent PoC",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // System Services Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("System Hooks Status", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                ServiceStatusRow(
                    title = "Accessibility View Mirror",
                    active = isAccessibilityActive,
                    actionLabel = "Settings",
                    onAction = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                ServiceStatusRow(
                    title = "Notification Listener",
                    active = true,
                    actionLabel = "Enable",
                    onAction = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                ServiceStatusRow(
                    title = "Shizuku ADB Privilege",
                    active = isShizukuActive,
                    actionLabel = "Request",
                    onAction = {
                        if (ShizukuBridge.isShizukuAvailable()) {
                            ShizukuBridge.requestPermission(1001)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Floating Overlay Controller
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Floating Co-pilot Overlay", fontWeight = FontWeight.SemiBold)
            Button(
                onClick = {
                    if (Settings.canDrawOverlays(context)) {
                        val intent = Intent(context, FloatingAgentService::class.java)
                        context.startService(intent)
                    } else {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                        context.startActivity(intent)
                    }
                }
            ) {
                Text("Launch Overlay")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cross-App Memory Q&A
        Text(
            text = "Ask Cross-App Co-pilot",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Questions that require recalling context across LINE, Notes, Browser, etc.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userQuestion,
            onValueChange = { userQuestion = it },
            label = { Text("e.g., สรุปคอนโดที่คุยกับนายหน้า, ที่อยู่ที่เพื่อนส่งมา") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (userQuestion.isNotBlank()) {
                            isQuerying = true
                            scope.launch {
                                val relevant = repository.searchMemory(userQuestion, limit = 5)
                                val prompt = PromptTemplates.buildMemoryRecallPrompt(userQuestion, relevant)
                                aiAnswer = "Prompt synthesized from ${relevant.size} local memory snapshots:\n\n$prompt"
                                isQuerying = false
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Ask")
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (isQuerying) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        aiAnswer?.let { answer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Co-pilot Memory Retrieval Result:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = answer,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceStatusRow(
    title: String,
    active: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (active) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (active) Color(0xFF2E7D32) else Color(0xFFE65100),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = title, fontSize = 14.sp)
        }
        if (!active) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text(actionLabel, fontSize = 12.sp)
            }
        } else {
            Text("Active", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
        }
    }
}

