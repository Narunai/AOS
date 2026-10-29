package org.agentos.poc.agent.orchestrator

import org.agentos.poc.memory.db.EpisodicSnapshotEntity

object PromptTemplates {

    fun buildMemoryRecallPrompt(
        userQuery: String,
        relevantSnapshots: List<EpisodicSnapshotEntity>
    ): String {
        val memoryContext = if (relevantSnapshots.isEmpty()) {
            "(No relevant episodic history found on this device)"
        } else {
            relevantSnapshots.joinToString("\n---\n") { snapshot ->
                """
                [Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(snapshot.timestampMs)}]
                App: ${snapshot.appLabel} (${snapshot.packageName})
                Screen Text: ${snapshot.extractedText.take(500)}
                """.trimIndent()
            }
        }

        return """
        You are the System Co-pilot of an Agentic Mobile OS.
        You have direct access to the user's cross-app episodic memory retrieved from local SQLite storage.
        
        ### User Question:
        $userQuery
        
        ### Retrieved Local Memory Context:
        $memoryContext
        
        ### Instructions:
        1. Answer the user directly, accurately synthesizing information across apps (e.g., chat messages, notes, browser pages).
        2. If specific dates, prices, addresses, or file names were found, state them clearly.
        3. Respond in polite Thai (or the language the user asked in).
        """.trimIndent()
    }

    fun buildActionPlanPrompt(
        userGoal: String,
        currentScreenPrompt: String,
        memoryContext: String
    ): String {
        return """
        You are the Autonomous Action Planner for Agentic Mobile OS.
        Your goal is to inspect the current screen's Compact Semantic DOM and decide the next action.
        
        ### Goal:
        $userGoal
        
        ### Current Screen DOM:
        $currentScreenPrompt
        
        ### Related Memory Context:
        $memoryContext
        
        ### Available Actions (Output as JSON):
        - {"action": "click", "nodeId": <id>, "label": "<label>"}
        - {"action": "input", "nodeId": <id>, "text": "<text>"}
        - {"action": "launch", "packageName": "<package>", "deepLink": "<uri>"}
        - {"action": "finish", "message": "<completion message>"}
        
        Output ONLY the next JSON action.
        """.trimIndent()
    }
}

