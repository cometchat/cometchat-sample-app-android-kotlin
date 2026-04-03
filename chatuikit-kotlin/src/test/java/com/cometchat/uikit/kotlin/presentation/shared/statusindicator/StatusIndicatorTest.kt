package com.cometchat.uikit.kotlin.presentation.shared.statusindicator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for StatusIndicator enum.
 */
class StatusIndicatorTest {

    @Test
    fun `StatusIndicator should have ONLINE value`() {
        val status = StatusIndicator.ONLINE
        assertNotNull(status)
        assertEquals("ONLINE", status.name)
    }

    @Test
    fun `StatusIndicator should have OFFLINE value`() {
        val status = StatusIndicator.OFFLINE
        assertNotNull(status)
        assertEquals("OFFLINE", status.name)
    }

    @Test
    fun `StatusIndicator should have PUBLIC_GROUP value`() {
        val status = StatusIndicator.PUBLIC_GROUP
        assertNotNull(status)
        assertEquals("PUBLIC_GROUP", status.name)
    }

    @Test
    fun `StatusIndicator should have PRIVATE_GROUP value`() {
        val status = StatusIndicator.PRIVATE_GROUP
        assertNotNull(status)
        assertEquals("PRIVATE_GROUP", status.name)
    }

    @Test
    fun `StatusIndicator should have PROTECTED_GROUP value`() {
        val status = StatusIndicator.PROTECTED_GROUP
        assertNotNull(status)
        assertEquals("PROTECTED_GROUP", status.name)
    }

    @Test
    fun `StatusIndicator should have exactly 5 values`() {
        val values = StatusIndicator.entries
        assertEquals(5, values.size)
    }

    @Test
    fun `StatusIndicator values should be in correct order`() {
        val values = StatusIndicator.entries
        assertEquals(StatusIndicator.ONLINE, values[0])
        assertEquals(StatusIndicator.OFFLINE, values[1])
        assertEquals(StatusIndicator.PUBLIC_GROUP, values[2])
        assertEquals(StatusIndicator.PRIVATE_GROUP, values[3])
        assertEquals(StatusIndicator.PROTECTED_GROUP, values[4])
    }

    @Test
    fun `StatusIndicator valueOf should return correct enum`() {
        assertEquals(StatusIndicator.ONLINE, StatusIndicator.valueOf("ONLINE"))
        assertEquals(StatusIndicator.OFFLINE, StatusIndicator.valueOf("OFFLINE"))
        assertEquals(StatusIndicator.PUBLIC_GROUP, StatusIndicator.valueOf("PUBLIC_GROUP"))
        assertEquals(StatusIndicator.PRIVATE_GROUP, StatusIndicator.valueOf("PRIVATE_GROUP"))
        assertEquals(StatusIndicator.PROTECTED_GROUP, StatusIndicator.valueOf("PROTECTED_GROUP"))
    }

    @Test
    fun `StatusIndicator ordinal values should be sequential`() {
        assertEquals(0, StatusIndicator.ONLINE.ordinal)
        assertEquals(1, StatusIndicator.OFFLINE.ordinal)
        assertEquals(2, StatusIndicator.PUBLIC_GROUP.ordinal)
        assertEquals(3, StatusIndicator.PRIVATE_GROUP.ordinal)
        assertEquals(4, StatusIndicator.PROTECTED_GROUP.ordinal)
    }

    @Test
    fun `StatusIndicator entries should contain all statuses`() {
        val entries = StatusIndicator.entries
        assertTrue(entries.contains(StatusIndicator.ONLINE))
        assertTrue(entries.contains(StatusIndicator.OFFLINE))
        assertTrue(entries.contains(StatusIndicator.PUBLIC_GROUP))
        assertTrue(entries.contains(StatusIndicator.PRIVATE_GROUP))
        assertTrue(entries.contains(StatusIndicator.PROTECTED_GROUP))
    }

    @Test
    fun `user statuses should be ONLINE and OFFLINE`() {
        val userStatuses = listOf(StatusIndicator.ONLINE, StatusIndicator.OFFLINE)
        assertEquals(2, userStatuses.size)
    }

    @Test
    fun `group statuses should be PUBLIC_GROUP, PRIVATE_GROUP, and PROTECTED_GROUP`() {
        val groupStatuses = listOf(
            StatusIndicator.PUBLIC_GROUP,
            StatusIndicator.PRIVATE_GROUP,
            StatusIndicator.PROTECTED_GROUP
        )
        assertEquals(3, groupStatuses.size)
    }
}
