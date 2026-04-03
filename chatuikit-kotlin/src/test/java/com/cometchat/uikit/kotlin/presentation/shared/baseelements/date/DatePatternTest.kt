package com.cometchat.uikit.kotlin.presentation.shared.baseelements.date

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for DatePattern enum.
 */
class DatePatternTest {

    @Test
    fun `DatePattern should have TIME value`() {
        val pattern = DatePattern.TIME
        assertNotNull(pattern)
        assertEquals("TIME", pattern.name)
    }

    @Test
    fun `DatePattern should have DAY_DATE value`() {
        val pattern = DatePattern.DAY_DATE
        assertNotNull(pattern)
        assertEquals("DAY_DATE", pattern.name)
    }

    @Test
    fun `DatePattern should have DAY_DATE_TIME value`() {
        val pattern = DatePattern.DAY_DATE_TIME
        assertNotNull(pattern)
        assertEquals("DAY_DATE_TIME", pattern.name)
    }

    @Test
    fun `DatePattern should have exactly 3 values`() {
        val values = DatePattern.entries
        assertEquals(3, values.size)
    }

    @Test
    fun `DatePattern values should be in correct order`() {
        val values = DatePattern.entries
        assertEquals(DatePattern.TIME, values[0])
        assertEquals(DatePattern.DAY_DATE, values[1])
        assertEquals(DatePattern.DAY_DATE_TIME, values[2])
    }

    @Test
    fun `DatePattern valueOf should return correct enum`() {
        assertEquals(DatePattern.TIME, DatePattern.valueOf("TIME"))
        assertEquals(DatePattern.DAY_DATE, DatePattern.valueOf("DAY_DATE"))
        assertEquals(DatePattern.DAY_DATE_TIME, DatePattern.valueOf("DAY_DATE_TIME"))
    }

    @Test
    fun `DatePattern ordinal values should be sequential`() {
        assertEquals(0, DatePattern.TIME.ordinal)
        assertEquals(1, DatePattern.DAY_DATE.ordinal)
        assertEquals(2, DatePattern.DAY_DATE_TIME.ordinal)
    }

    @Test
    fun `DatePattern entries should contain all patterns`() {
        val entries = DatePattern.entries
        assertTrue(entries.contains(DatePattern.TIME))
        assertTrue(entries.contains(DatePattern.DAY_DATE))
        assertTrue(entries.contains(DatePattern.DAY_DATE_TIME))
    }
}
