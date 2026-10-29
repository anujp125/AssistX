package com.nudge.assist.data.diagnostics

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local diagnostic events with bounded retention pruning.
 */
@Dao
interface DiagnosticDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: DiagnosticEventEntity): Long

    @Query("SELECT * FROM diagnostic_events ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun getRecentEvents(limit: Int = 100): Flow<List<DiagnosticEventEntity>>

    @Query("SELECT COUNT(*) FROM diagnostic_events")
    suspend fun getEventCount(): Int

    /**
     * Bounded FIFO retention: keeps only the newest keepCount events and deletes older records.
     */
    @Query("DELETE FROM diagnostic_events WHERE id NOT IN (SELECT id FROM diagnostic_events ORDER BY timestamp DESC, id DESC LIMIT :keepCount)")
    suspend fun pruneOldEvents(keepCount: Int = 500): Int

    @Query("DELETE FROM diagnostic_events")
    suspend fun clearAllEvents(): Int
}

