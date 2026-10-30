package com.assistx.data

import com.assistx.data.diagnostics.DiagnosticDao
import com.assistx.data.diagnostics.DiagnosticEventEntity
import com.assistx.data.diagnostics.DiagnosticRecorder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeDiagnosticDao : DiagnosticDao {
    val events = mutableListOf<DiagnosticEventEntity>()
    private val eventsFlow = MutableStateFlow<List<DiagnosticEventEntity>>(emptyList())
    var shouldThrowOnInsert = false

    override suspend fun insertEvent(event: DiagnosticEventEntity): Long {
        if (shouldThrowOnInsert) {
            throw RuntimeException("Simulated SQLite disk error")
        }
        val id = (events.size + 1).toLong()
        val saved = event.copy(id = id)
        events.add(0, saved)
        eventsFlow.value = events.toList()
        return id
    }

    override fun getRecentEvents(limit: Int): Flow<List<DiagnosticEventEntity>> = eventsFlow

    override suspend fun getEventCount(): Int = events.size

    override suspend fun pruneOldEvents(keepCount: Int): Int {
        if (events.size > keepCount) {
            val removed = events.size - keepCount
            while (events.size > keepCount) {
                events.removeAt(events.lastIndex)
            }
            eventsFlow.value = events.toList()
            return removed
        }
        return 0
    }

    override suspend fun clearAllEvents(): Int {
        val count = events.size
        events.clear()
        eventsFlow.value = emptyList()
        return count
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DiagnosticRecorderTest {

    private lateinit var fakeDao: FakeDiagnosticDao
    private lateinit var testScope: TestScope
    private lateinit var recorder: DiagnosticRecorder

    @Before
    fun setup() {
        fakeDao = FakeDiagnosticDao()
        val testDispatcher = StandardTestDispatcher()
        testScope = TestScope(testDispatcher)
        recorder = DiagnosticRecorder(dao = fakeDao, scope = testScope)
    }

    @Test
    fun testRecordEventInsertsAndPrunes() = testScope.runTest {
        recorder.recordEvent(
            eventType = "STATE_CHANGE",
            component = "OVERLAY",
            result = "SUCCESS",
            details = "Attached successfully"
        )
        advanceUntilIdle()

        assertEquals(1, fakeDao.events.size)
        assertEquals("STATE_CHANGE", fakeDao.events.first().eventType)
        assertEquals("OVERLAY", fakeDao.events.first().component)
        assertEquals("SUCCESS", fakeDao.events.first().result)
    }

    @Test
    fun testRecordEventBestEffortDoesNotCrashOnDbException() = testScope.runTest {
        fakeDao.shouldThrowOnInsert = true

        // Should not throw exception
        recorder.recordEvent(
            eventType = "FAILED_ATTEMPT",
            component = "DATABASE"
        )
        advanceUntilIdle()

        assertTrue(fakeDao.events.isEmpty())
    }

    @Test
    fun testClearAllDeletesEvents() = testScope.runTest {
        fakeDao.events.add(DiagnosticEventEntity(id = 1, eventType = "E1", component = "C1"))
        fakeDao.events.add(DiagnosticEventEntity(id = 2, eventType = "E2", component = "C2"))

        recorder.clearAll()

        assertTrue(fakeDao.events.isEmpty())
    }
}

