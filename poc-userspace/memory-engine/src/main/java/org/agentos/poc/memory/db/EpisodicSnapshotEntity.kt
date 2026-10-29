package org.agentos.poc.memory.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "episodic_snapshots")
data class EpisodicSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val id: Long = 0,

    @ColumnInfo(name = "snapshot_id")
    val snapshotId: String,

    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long,

    @ColumnInfo(name = "package_name")
    val packageName: String,

    @ColumnInfo(name = "app_label")
    val appLabel: String,

    @ColumnInfo(name = "activity_name")
    val activityName: String?,

    @ColumnInfo(name = "window_title")
    val windowTitle: String?,

    @ColumnInfo(name = "compact_dom_json")
    val compactDomJson: String,

    @ColumnInfo(name = "extracted_text")
    val extractedText: String,

    @ColumnInfo(name = "ai_summary")
    val aiSummary: String? = null,

    @ColumnInfo(name = "sim_hash")
    val simHash: Long,

    @ColumnInfo(name = "content_hash")
    val contentHash: String
)

@Fts4(contentEntity = EpisodicSnapshotEntity::class)
@Entity(tableName = "episodic_fts")
data class EpisodicFtsEntity(
    @ColumnInfo(name = "app_label")
    val appLabel: String,

    @ColumnInfo(name = "window_title")
    val windowTitle: String?,

    @ColumnInfo(name = "extracted_text")
    val extractedText: String,

    @ColumnInfo(name = "ai_summary")
    val aiSummary: String?
)

