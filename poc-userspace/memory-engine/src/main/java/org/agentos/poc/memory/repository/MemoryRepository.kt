package org.agentos.poc.memory.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.agentos.poc.collector.model.CompactDom
import org.agentos.poc.memory.db.AgentDatabase
import org.agentos.poc.memory.db.EpisodicSnapshotEntity
import org.agentos.poc.memory.search.SimHashDeduplicator

class MemoryRepository(context: Context) {

    private val dao = AgentDatabase.getInstance(context).episodicDao()
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    val recentSnapshotsFlow: Flow<List<EpisodicSnapshotEntity>> = dao.getRecentSnapshotsFlow()

    /**
     * Ingests a new Compact DOM snapshot into episodic memory.
     * Uses SimHash to skip near-duplicate screens (>85% identical).
     */
    suspend fun recordSnapshot(
        dom: CompactDom,
        appLabel: String
    ): Boolean {
        val extractedText = dom.nodes.joinToString(" ") { "${it.text} ${it.contentDescription.orEmpty()}" }.trim()
        if (extractedText.isBlank()) return false

        val currentSimHash = SimHashDeduplicator.computeSimHash(extractedText)
        val lastSimHash = dao.getLastSimHash()

        if (lastSimHash != null && SimHashDeduplicator.isNearDuplicate(lastSimHash, currentSimHash)) {
            // Near duplicate (> 85% match), skip to save disk space and battery
            return false
        }

        val entity = EpisodicSnapshotEntity(
            snapshotId = dom.snapshotId,
            timestampMs = dom.timestampMs,
            packageName = dom.packageName,
            appLabel = appLabel,
            activityName = dom.activityName,
            windowTitle = dom.windowTitle,
            compactDomJson = json.encodeToString(dom),
            extractedText = extractedText,
            simHash = currentSimHash,
            contentHash = dom.contentHash
        )

        dao.insertSnapshot(entity)
        return true
    }

    suspend fun searchMemory(query: String, limit: Int = 15): List<EpisodicSnapshotEntity> {
        val sanitizedQuery = query.trim().replace(Regex("[*\"']"), "")
        if (sanitizedQuery.isBlank()) return emptyList()

        return try {
            // Try FTS prefix search (e.g. "คอนโด*")
            dao.searchFts("$sanitizedQuery*", limit)
        } catch (e: Exception) {
            // Fallback to recent snapshots if query syntax fails
            dao.getRecentSnapshots(limit)
        }
    }

    suspend fun deleteSnapshot(id: Long) {
        dao.deleteSnapshotById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}

