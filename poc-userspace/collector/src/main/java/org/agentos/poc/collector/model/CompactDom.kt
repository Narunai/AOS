package org.agentos.poc.collector.model

import kotlinx.serialization.Serializable

@Serializable
data class CompactDom(
    val snapshotId: String,
    val timestampMs: Long,
    val packageName: String,
    val activityName: String? = null,
    val windowTitle: String? = null,
    val screenSummaryHint: String? = null,
    val contentHash: String,
    val nodes: List<NodeItem> = emptyList()
) {
    /**
     * Converts DOM into a token-efficient string representation for LLM context windows.
     */
    fun toPromptString(): String {
        val sb = StringBuilder()
        sb.appendLine("App: $packageName | Activity: ${activityName ?: "Unknown"}")
        if (!windowTitle.isNullOrBlank()) {
            sb.appendLine("Window Title: $windowTitle")
        }
        sb.appendLine("Interactive Elements:")
        for (node in nodes) {
            val interactableFlags = buildList {
                if (node.clickable) add("clickable")
                if (node.editable) add("editable")
                if (node.scrollable) add("scrollable")
            }.joinToString(",")

            val desc = if (!node.contentDescription.isNullOrBlank()) " desc='${node.contentDescription}'" else ""
            val idRef = if (!node.viewIdResourceName.isNullOrBlank()) " idRef='${node.viewIdResourceName}'" else ""

            sb.appendLine("  [#${node.id}] ${node.role} text='${node.text}'$desc$idRef ($interactableFlags)")
        }
        return sb.toString()
    }
}

