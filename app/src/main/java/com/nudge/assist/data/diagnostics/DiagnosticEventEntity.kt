package com.nudge.assist.data.diagnostics

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Immutable entity representing a local diagnostic observation in AssistX.
 * Zero network transmission; maximum 500 events bounded retention.
 */
@Entity(tableName = "diagnostic_events")
data class DiagnosticEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val severity: String = "INFO",
    val component: String,
    val result: String = "SUCCESS",
    val details: String = ""
)
