package org.agentos.poc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.agentos.poc.privacy.BankingPackageRegistry
import org.agentos.poc.privacy.PiiRegexMasker

@Composable
fun PrivacyDashboardScreen() {
    var testInput by remember { mutableStateOf("โอนเงินให้ นายสมชาย 1-2345-67890-12-1 รหัส OTP ของคุณคือ 849204 บัตรเครดิต 4111 2222 3333 4444") }
    var maskedOutput by remember { mutableStateOf(PiiRegexMasker.maskAll(testInput)) }
    val blacklistedPackages = remember { BankingPackageRegistry.getBlacklist().toList() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Privacy & Sensitive Shield",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Deterministic, on-device sanitization active before data touches storage or models.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Live PII Masking Tester", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = testInput,
                    onValueChange = {
                        testInput = it
                        maskedOutput = PiiRegexMasker.maskAll(it)
                    },
                    label = { Text("Enter test text containing sensitive info") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sanitized Output (What AI sees):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text = maskedOutput,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Auto-Blacklisted Packages (${blacklistedPackages.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Observation is completely disabled for these banking and password apps",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(blacklistedPackages) { pkg ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = pkg,
                        modifier = Modifier.padding(8.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

