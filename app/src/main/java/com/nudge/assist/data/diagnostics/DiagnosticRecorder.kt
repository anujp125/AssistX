package com.nudge.assist.data.diagnostics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

/**
 * Interface contract for recording and querying local diagnostics.
 */
interface DiagnosticRecorderContract {
    fun recordEvent(
        eventType: String,
        component: String,
        result: String = "SUCCESS",
        severity: String = "INFO",
        details: String = ""
    )
    fun getRecentEvents(limit: Int = 100): Flow<List<DiagnosticEventEntity>>
    suspend fun clearAll()
}

/**
 * Concrete recorder with bounded FIFO retention pruning (max 500 events).
 * Best-effort logging: failures never crash callers.
 */
class DiagnosticRecorder(
    private val dao: DiagnosticDao?,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : DiagnosticRecorderContract {

    override fun recordEvent(
        eventType: String,
        component: String,
        result: String,
        severity: String,
        details: String
    ) {
        if (dao == null) return
        scope.launch {
            try {
                dao.insertEvent(
                    DiagnosticEventEntity(
                        eventType = eventType,
                        component = component,
                        result = result,
                        severity = severity,
                        details = sanitizeDetails(details)
                    )
                )
                dao.pruneOldEvents(500)
            } catch (e: Exception) {
                // Best effort logging: never crash caller
            }
        }
    }

    private fun sanitizeDetails(rawDetails: String): String {
        if (rawDetails.isEmpty()) return ""
        val trimmed = if (rawDetails.length > 256) rawDetails.substring(0, 256) else rawDetails
        return trimmed
            .replace(Regex("""/data/user/\d+/[^/\s]+"""), "/data/user/[REDACTED]")
            .replace(Regex("""[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}"""), "[EMAIL_REDACTED]")
    }

    override fun getRecentEvents(limit: Int): Flow<List<DiagnosticEventEntity>> {
        return dao?.getRecentEvents(limit) ?: emptyFlow()
    }

    override suspend fun clearAll() {
        try {
            dao?.clearAllEvents()
        } catch (e: Exception) {
            // Ignored
        }
    }
}

