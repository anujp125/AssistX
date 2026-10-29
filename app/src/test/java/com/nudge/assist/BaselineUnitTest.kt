package com.nudge.assist

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BaselineUnitTest {

    @Test
    fun testNotificationChannelConfigured() {
        assertNotNull(AssistApplication.NOTIFICATION_CHANNEL_ID)
        assertEquals("assistx_foreground_service", AssistApplication.NOTIFICATION_CHANNEL_ID)
    }

    @Test
    fun testApplicationIdMatchesSpecification() {
        val expectedPackage = "com.nudge.assist"
        val actualPackage = AssistApplication::class.java.`package`?.name
        assertEquals(expectedPackage, actualPackage)
    }
}

