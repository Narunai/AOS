package org.agentos.poc.collector.model

import kotlinx.serialization.Serializable

@Serializable
data class EventNotification(
    val id: String,
    val timestampMs: Long,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val category: String? = null,
    val hasInlineReply: Boolean = false
)

