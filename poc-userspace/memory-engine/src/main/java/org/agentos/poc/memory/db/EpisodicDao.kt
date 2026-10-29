package org.agentos.poc.memory.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: EpisodicSnapshotEntity): Long

    @Query("SELECT * FROM episodic_snapshots ORDER BY timestamp_ms DESC LIMIT :limit")
    fun getRecentSnapshotsFlow(limit: Int = 50): Flow<List<EpisodicSnapshotEntity>>

    @Query("SELECT * FROM episodic_snapshots ORDER BY timestamp_ms DESC LIMIT :limit")
    suspend fun getRecentSnapshots(limit: Int = 50): List<EpisodicSnapshotEntity>

    @Query("SELECT * FROM episodic_snapshots WHERE timestamp_ms BETWEEN :startTimeMs AND :endTimeMs ORDER BY timestamp_ms DESC")
    suspend fun getSnapshotsInTimeRange(startTimeMs: Long, endTimeMs: Long): List<EpisodicSnapshotEntity>

    @Query("SELECT * FROM episodic_snapshots WHERE package_name = :packageName ORDER BY timestamp_ms DESC LIMIT :limit")
    suspend fun getSnapshotsByPackage(packageName: String, limit: Int = 20): List<EpisodicSnapshotEntity>

    /**
     * High-speed Full-Text Search (FTS) query joining the FTS virtual table with episodic_snapshots.
     */
    @Query("""
        SELECT s.* FROM episodic_snapshots s
        JOIN episodic_fts f ON s.rowid = f.rowid
        WHERE episodic_fts MATCH :searchQuery
        ORDER BY s.timestamp_ms DESC
        LIMIT :limit
    """)
    suspend fun searchFts(searchQuery: String, limit: Int = 20): List<EpisodicSnapshotEntity>

    @Query("SELECT sim_hash FROM episodic_snapshots ORDER BY timestamp_ms DESC LIMIT 1")
    suspend fun getLastSimHash(): Long?

    @Query("DELETE FROM episodic_snapshots WHERE rowid = :id")
    suspend fun deleteSnapshotById(id: Long)

    @Query("DELETE FROM episodic_snapshots WHERE timestamp_ms < :cutoffTimestampMs")
    suspend fun deleteOlderThan(cutoffTimestampMs: Long): Int

    @Query("DELETE FROM episodic_snapshots")
    suspend fun clearAll()
}

