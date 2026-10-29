package org.agentos.poc.collector.model

import kotlinx.serialization.Serializable

@Serializable
data class NodeItem(
    val id: Int,
    val role: String, // e.g. "button", "input", "text", "header", "image", "switch"
    val text: String,
    val contentDescription: String? = null,
    val viewIdResourceName: String? = null,
    val clickable: Boolean = false,
    val editable: Boolean = false,
    val scrollable: Boolean = false,
    val bounds: List<Int> = emptyList() // [left, top, right, bottom]
)

